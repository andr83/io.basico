package io.basico.driver

import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.generic.CanBuildFrom

/**
  * @author Andrei Tupitcyn
  */
trait ResultReader[A, D <: DriverConf] {
  def read(publisher: Publisher[Row[D]]): A
}

object ResultReader {
  implicit def optionalResultReader[A, D <: DriverConf](
    implicit rowReader: RowReader[A, D]
  ): ResultReader[Option[A], D] =
    new ResultReader[Option[A], D] {
      override def read(publisher: Publisher[Row[D]]): Option[A] = {
        var res: Option[A] = None

        val subscriber: Subscriber[Row[D]] = new Subscriber[Row[D]] {
          private var sub: Subscription = _

          override def onError(t: Throwable): Unit = throw t

          override def onComplete(): Unit = {}

          override def onNext(t: Row[D]): Unit = {
            res = Some(rowReader.read(t))
            sub.cancel()
          }

          override def onSubscribe(s: Subscription): Unit = {
            sub = s
            s.request(1)
          }
        }
        publisher.subscribe(subscriber)
        res
      }
    }

  implicit def singleResultReader[A, D <: DriverConf](
    implicit optReader: ResultReader[Option[A], D]
  ): ResultReader[A, D] =
    new ResultReader[A, D] {
      override def read(publisher: Publisher[Row[D]]): A =
        optReader.read(publisher).get
    }

  implicit def seqResultReader[A, C[_], D <: DriverConf](implicit rowReader: RowReader[A, D],
                                                         cbf: CanBuildFrom[Nothing, A, C[A]]): ResultReader[C[A], D] =
    new ResultReader[C[A], D] {
      override def read(publisher: Publisher[Row[D]]): C[A] = {
        val res = cbf()

        val subscriber: Subscriber[Row[D]] = new Subscriber[Row[D]] {

          override def onError(t: Throwable): Unit = throw t

          override def onComplete(): Unit = {}

          override def onNext(t: Row[D]): Unit = {
            res += rowReader.read(t)
          }

          override def onSubscribe(s: Subscription): Unit = {
            s.request(Long.MaxValue)
          }
        }
        publisher.subscribe(subscriber)
        res.result()
      }
    }
}
