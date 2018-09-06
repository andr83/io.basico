package io.basico.jdbc

import java.sql.{Connection, PreparedStatement, ResultSet}

import io.basico.driver.Query.{ReturnGeneratedKeys, ReturnKind, Returning}
import io.basico.driver.{QueryExecutor, RowReader}
import io.basico.util.resource.withResource
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.util.Try

/**
  * @author Andrei Tupitcyn
  */
class JdbcQueryExecutor(val connection: Connection) extends QueryExecutor[JdbcDriver] {

  override def execute[A](query: JdbcQuery, rowReader: RowReader[A, JdbcDriver]): Publisher[A] = {
    new Publisher[A] {
      override def subscribe(s: Subscriber[_ >: A]): Unit = {
        s.onSubscribe(new RowSubscription(s))
      }

      class RowSubscription(s: Subscriber[_ >: A]) extends Subscription {

        var count = 0
        lazy val (statement: PreparedStatement, rs: ResultSet, nextRow: (() => A)) = {
          val stm = connection.prepareStatement(query.sql)
          var rs: ResultSet = null
          Try {
            query.holder.binder(query.holder.value).bind(stm, 1)
            stm.execute()
            rs = stm.getResultSet
          } recover {
            case e =>
              if (rs != null) {
                Try(rs.close())
              }
              stm.close()
              throw e
          }
          (stm, rs, rowReader.build(rs))
        }

        override def cancel(): Unit = {
          close()
        }

        override def request(n: Long): Unit = {
          if (n < 1) {
            terminate(new IllegalArgumentException(s"$s request non positive number of elements: $n."))
          } else {
            Try {
              var i = 0
              while (i < n && rs.next()) {
                s.onNext(nextRow())
                i += 1
              }

              if (i < n) {
                s.onComplete()
              }
            } recover {
              case e => terminate(e)
            }
          }
        }

        def close(): Unit = {
          Try(rs.close())
          Try(statement.close())
        }

        def terminate(error: Throwable): Unit = {
          Try(s.onError(error)).recover {
            case e =>
              new IllegalStateException(s"$s threw an exception from onError", e).printStackTrace()
          }
          close()
        }
      }
    }
  }

  override def update(query: JdbcQuery): Int = {
    withResource(connection.prepareStatement(query.sql)) { stm =>
      query.holder.binder(query.holder.value).bind(stm, 1)
      stm.executeUpdate()
    }
  }

  override def updateAndReturn[A](query: JdbcQuery, returnKind: ReturnKind, rowReader: JdbcRowReader[A]): A = {
    withResource {
      returnKind match {
        case ReturnGeneratedKeys => connection.prepareStatement(query.sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        case Returning           => connection.prepareStatement(query.sql)
      }
    } { stm =>
      query.holder.binder(query.holder.value).bind(stm, 1)
      stm.execute()
      val rs = returnKind match {
        case ReturnGeneratedKeys => stm.getGeneratedKeys
        case Returning           => stm.getResultSet
      }

      rs.next()
      rowReader.build(rs)()
    }
  }
}
