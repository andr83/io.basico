# io.basico
[![Build Status](https://travis-ci.org/andr83/io.basico.svg?branch=master)](https://travis-ci.org/andr83/io.basico)

**Basico** is a scala functional database access layer.

## Overview
It's a research project to write a generic Scala library which provides direct access to any **SQL** database in a functional 
way. There are a lot of implementations of such libraries like [Doobie](https://github.com/tpolecat/doobie), 
[ScalikeJDBC](http://scalikejdbc.org), [Anorm](https://github.com/playframework/anorm), etc. But all of them based
on top of **JDBC** and you have no options to use alternative driver implementations. **Doobie** has a strong dependency 
on **Cats** and **FS2** libraries also and it's another point of concern.

The main criteria for **Basico** are

* independence from any concrete driver implementation
* support both `synchronous` and `asynchronous` access
* streaming support
* developer-friendly API
* minimum of external dependencies
* simplicity and extensibility

Because of difference in implementation in concrete drivers we need to know some types of there internal API's. And all 
**Basico** instances must be compatible only for the concrete driver. To solve this problem was added driver configuration 
trait `DriverConf`:

```scala
trait DriverConf {
  type ResultSet // what driver's result type is
  type ParameterBinder // driver's parameter binder type
}
```

An example of implementation for `JDBC`:
```scala
class JdbcDriver extends DriverConf {
  override type ResultSet = java.sql.ResultSet
  override type ParameterBinder = PreparedStatement
}
```

All `SQL` queries represented with `Query` trait:
```scala
trait Query[D <: DriverConf] {
  type A // Type of binded to query value
  def holder: Query.ParameterHolder[A, D] // Holder of the binded value of type A
  def sql: String // SQL fragment
}
``` 

`Query` used to build a program `DbIO` and after run it with a `QueryExecutor`. To support streaming and `asynchronous` 
runners `QueryExecutor` return result via [Reactive Streams](https://github.com/reactive-streams/reactive-streams-jvm) 
Publisher. To get final result there is a `ResultReader` typeclass:

```scala
trait ResultReader[A, D <: DriverConf] {
  def read(publisher: Publisher[Row[D]]): A
}
```

## Quick start
### Query data
Will use **JDBC** driver implementation in examples.

```scala
import javax.sql.DataSource
import io.basico.jdbc._
import io.basico.jdbc.implicits._


val ds: DataSource = _ // Some connection data source

ds.withQueryExecutor {implicit queryExecutor=>
  val users = 
    sql"select name from users" // Query[JdbcDriver]
        .as[List[String]]       // QueryIO[List[String], JdbcDriver]
                                // (implicit resultReader: ResultReader[List[String], JdbcDriver], 
                                //           rowReader[String]: RowReader[String, JdbcDriver], 
                                //           columnReader: ColumnReader[String, JdbcDriver])
        .unsafeRunSync          // List[String]
}
```

What happens here:

* `sql"select name from users"` we use scala string interpolation to build a `Query[JdbcDriver]` instance.
* `.as[List[String]]` convert `Query` instance to `QueryIO` monad which can be run later to materialize result value. To
make it works must be `implicitly` available `ResultReader[List[String], JdbcDriver]` instance. There is a default 
implementation for any collection type that has a `CanBuildFrom` but it `implicitly` require 
`RowReader[String, JdbcDriver]` which derived from `ColumnReader[String, JdbcDriver]` and read the only first column in 
result set.
* `unsafeRunSync` execute a query and return a result. It `implicitly` requires `QueryExecutor[JdbcDriver]` instance. 

### Parameteres

As it was mentioned above query can be parametrized in multiple ways. It can be done with string interpolation:

 ```scala
 val minAge = 7
 val maxAge = 18
 val schoolchilds = sql"select name from users where age > $minAge and age < $maxAge"
                      .as[List[String]]
                      .unsafeRunSync
 ```
 
It will create the next `Query` instance:
 
 ```scala
new Query[JdbcQuery] {
  type A = (Int, Int)
  val holder = Query.ParameterHolder((7, 18), implicitly[ValueBinder[(Int, Int)]])
  def sql = "select name from users where age > ? and age < ?"
}
```

`ValueBinder` here is a typeclass implementation of which knows how to bind the value of type `A` to 
`DriverConf.ParameterBinder`. There are implementations for the most common Scala types.

Parameters can be also bind to the `IN` section:

```scala
val names = Seq("Oleg", "Ivan", "Sergey")
val winners = (sql"select name, age from users where " ++ Fragments.in("name", names))
  .as[List[(String, Int)]]
  .unsafeRunSync
```

### DDL queries

`Query` can be used for **DDL** queries such **Insert** or **Update** as well.

```scala
def addUser(name: String, age: Int): UpdateIO[JdbcDriver] = {
  sql"""
    |INSERT INTO users (name, age)
    |VALUES ($name, $age)
  """.stripMargin.update
}

addUser("Andrey", 25).unsafeRunSync
```

Here we run method `update` on `Query` instance and that return us `UpdateIO[JdbcDriver]`. Both `QueryIO` and `UpdateIO`
are implementation of base `DbIO` monad.

Some databases allow return auto generated id on an insert.

```scala
def addUser(name: String, age: Int): UpdateAndGetGeneratedKeysIO[Int, JdbcDriver] = {
  sql"""
    |INSERT INTO users (name, age)
    |VALUES ($name, $age)
  """.stripMargin.update.withGeneratedKeys[Int]
}

val userId: Int = addUser("Andrey", 25).unsafeRunSync
```

### Composition

Because `DbIO` is a monad it can be composed in sequential flow and after run all-at-once.

```scala
case class User(id: Int, name: String, age: Int)

def addUser(name: String, age: Int): DbIO[User, JdbcDriver] = {
  for {
    id <- sql"insert into users (name, age) values ($name, $age)".update.withGeneratedKeys[Int]
    (name, age) <- sql"select name, age from users where id = $id".as[(String, Int)]
  } yield User(id, name, age)
}

val program = for {
  user1 <- addUser("Ivan", 20)
  user2 <- addUser("Masha", 18)
} yield user1 :: user2 :: Nil

val users: List[User] = program.unsafeRunSync
```

### Lifting execution to context

In real application would like to isolate all our side-effects and optimize async and parallel executions with some 
context. It can be any `IO` monad implementation like `cats-effect` or Monix `Task` or even simple Scala `Future`.

Any `DbIO` instance can be lifted to external execution context if an instance of `LiftIO` typeclass is available.
By default provided the only implementation for Scala `Future`.

```scala
val users: Future[List[String]] = sql"select name from users".as[List[String]].liftF[Future]
```

## License

MIT License

Copyright (c) 2018 Andrei Tupitcyn