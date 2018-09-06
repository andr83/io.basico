package io.basico.io

import io.basico.driver.Query.ReturnGeneratedKeys
import io.basico.driver._

/**
  *
  * @author Andrei Tupitcyn
  */
sealed trait DbIO[A, D <: DriverConf] { self =>
  @inline def map[B](fa: A => B): DbIO[B, D] = new DbIO[B, D] {
    override def unsafeRunSync(implicit qe: QueryExecutor[D]): B = {
      val a = self.unsafeRunSync
      fa(a)
    }
  }

  @inline def flatMap[B](fa: A => DbIO[B, D]): DbIO[B, D] = new DbIO[B, D] {
    override def unsafeRunSync(implicit qe: QueryExecutor[D]): B = {
      val a = self.unsafeRunSync
      fa(a).unsafeRunSync
    }
  }

  def unsafeRunSync(implicit queryExecutor: QueryExecutor[D]): A

  def liftF[F[_]](implicit liftIO: LiftIO[F], queryExecutor: QueryExecutor[D]): F[A] =
    liftIO.liftIO(this, queryExecutor)
}

object DbIO {
  def id[A, D <: DriverConf](value: A): DbIO[A, D] = new DbIO[A, D] {
    override def unsafeRunSync(implicit queryExecutor: QueryExecutor[D]): A = value
  }

  case class QueryIO[A, D <: DriverConf](query: Query[D], resultReader: ResultReader[A, D]) extends DbIO[A, D] {
    def unsafeRunSync(implicit queryExecutor: QueryExecutor[D]): A = {
      val p = queryExecutor.execute(query, resultReader.rowReader)
      resultReader.read(p)
    }
  }

  case class UpdateIO[D <: DriverConf](query: Query[D]) extends DbIO[Int, D] {
    override def unsafeRunSync(implicit queryExecutor: QueryExecutor[D]): Int = queryExecutor.update(query)

    def withGeneratedKeys[A](implicit rowReader: RowReader[A, D]): UpdateAndGetGeneratedKeysIO[A, D] =
      UpdateAndGetGeneratedKeysIO[A, D](query, rowReader)
  }

  case class UpdateAndGetGeneratedKeysIO[A, D <: DriverConf](query: Query[D], rowReader: RowReader[A, D])
      extends DbIO[A, D] {
    override def unsafeRunSync(implicit queryExecutor: QueryExecutor[D]): A =
      queryExecutor.updateAndReturn(query, ReturnGeneratedKeys, rowReader)
  }
}
