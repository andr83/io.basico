package io.basico.driver

import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.generic.CanBuildFrom

/**
  * @author Andrei Tupitcyn
  */
trait ResultReader[Result, D <: DriverConf] {
  type Row
  def rowReader: RowReader[Row, D]
  def read(publisher: Publisher[Row]): Result
}

object ResultReader {
  implicit def optionalResultReader[A, D <: DriverConf](
    implicit _rowReader: RowReader[A, D]
  ): ResultReader[Option[A], D] =
    new ResultReader[Option[A], D] {
      override type Row = A
      override def rowReader: RowReader[A, D] = _rowReader
      override def read(publisher: Publisher[A]): Option[A] = {
        var res: Option[A] = None

        val subscriber: Subscriber[A] = new Subscriber[A] {
          private var sub: Subscription = _

          override def onError(t: Throwable): Unit = throw t

          override def onComplete(): Unit = {}

          override def onNext(a: A): Unit = {
            res = Some(a)
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

  implicit def singleResultReader[A, D <: DriverConf](implicit optReader: ResultReader[Option[A], D],
                                                      _rowReader: RowReader[A, D]): ResultReader[A, D] =
    new ResultReader[A, D] {
      override type Row = A
      override def rowReader: RowReader[A, D] = _rowReader
      override def read(publisher: Publisher[A]): A =
        optReader.read(publisher.asInstanceOf[Publisher[optReader.Row]]).get
    }

  implicit def seqResultReader[A, C[_], D <: DriverConf](implicit _rowReader: RowReader[A, D],
                                                         cbf: CanBuildFrom[Nothing, A, C[A]]): ResultReader[C[A], D] =
    new ResultReader[C[A], D] {
      override type Row = A
      override def rowReader: RowReader[A, D] = _rowReader
      override def read(publisher: Publisher[A]): C[A] = {
        val res = cbf()

        val subscriber: Subscriber[A] = new Subscriber[A] {

          override def onError(t: Throwable): Unit = throw t

          override def onComplete(): Unit = {}

          override def onNext(a: A): Unit = {
            res += a
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
