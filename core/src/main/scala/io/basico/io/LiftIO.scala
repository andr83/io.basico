package io.basico.io

import io.basico.driver.QueryExecutor
import io.basico.driver.DriverConf

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

import scala.annotation.implicitNotFound

/**
  * @author Andrei Tupitcyn
  */

@implicitNotFound("""Cannot find implicit value for LiftIO[${F}]. 
Building this implicit value might depend on having an implicit
s.c.ExecutionContext in scope, a Scheduler or some equivalent type.""")
trait LiftIO[F[_]] {
  def liftIO[A, D <: DriverConf](ioa: DbIO[A, D], queryExecutor: QueryExecutor[D]): F[A]
}

object LiftIO {
  // instance to lift [[DbIO]] execution to `scala.concurrent.Future`
  implicit def futureLiftIO(implicit ec: ExecutionContext): LiftIO[Future] =
    new LiftIO[Future] {
      override def liftIO[A, D <: DriverConf](ioa: DbIO[A, D], queryExecutor: QueryExecutor[D]): Future[A] =
        Future {
          ioa.unsafeRunSync(queryExecutor)
        }(ec)
    }
}
