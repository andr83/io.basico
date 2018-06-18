package io.basico.jdbc

import io.basico.jdbc.implicits._
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Andrei Tupitcyn
  */
class QuerySpec extends FlatSpec with Matchers with DbIOTest {

  "Query" should "be created using string interpolation" in {
    val min = 10
    val q = sql"select * from test where int_field > $min"
    q.sql shouldBe "select * from test where int_field > ?"
  }

  it should "be able run queries in monadic flow" in fromIO {
    for {
      res1 <- sql"select int_field from test where int_field = 10".as[Int]
      res2 <- sql"select string_field from test where int_field = ? or string_field = ?"
        .bind(10, "World")
        .as[List[String]]
        .map(_.mkString(" "))
      res3 <- (sql"select int_field from test where " ++ Fragments.in("int_field", 10, 11))
        .as[List[Int]]
        .map(_.sum)
      res4 <- sql"select array_field from test"
        .as[List[List[Int]]]
        .map(_.flatten)
    } yield {
      res1 shouldBe 10
      res2 shouldBe "Hello World"
      res3 shouldBe 21
      res4 shouldBe 1 :: 2 :: 3 :: 4 :: 5 :: 6 :: Nil
    }
  }

  it should "support returning auto generated keys" in fromIO {
    for {
      id <- sql"insert into test(int_field) values(20)".update.withGeneratedKeys[Int]
      v <- sql"select int_field from test where id= $id".as[Int]
    } yield {
      v shouldBe 20
    }
  }
}
