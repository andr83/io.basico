package io.basico.driver

/**
  * Instance to bind concrete value to driver `ParameterBinder`
  *
  * @author Andrei Tupitcyn
  */
trait ValueBinder[A, D <: DriverConf] extends (A => Binder[D]) { self =>
  def merge[B](other: ValueBinder[B, D]): ValueBinder[(A, B), D] = new ValueBinder[(A, B), D] {
    override def apply(ab: (A, B)): Binder[D] = new Binder[D] {
      override def bind(ps: D#ParameterBinder, pos: Int): Int = {
        val nextPos = self.apply(ab._1).bind(ps, pos)
        other.apply(ab._2).bind(ps, nextPos)
      }
    }
  }

  def ++[B](other: ValueBinder[B, D]): ValueBinder[(A, B), D] = merge(other)
}

object ValueBinder {
  def apply[A, D <: DriverConf](f: (A, D#ParameterBinder, Int) => Int): ValueBinder[A, D] =
    new ValueBinder[A, D] {
      override def apply(a: A): Binder[D] = new Binder[D] {
        override def bind(ps: D#ParameterBinder, index: Int): Int =
          f(a, ps, index)
      }
    }

  implicit def unitBinder[D <: DriverConf]: ValueBinder[Unit, D] = ValueBinder[Unit, D]((a, ps, pos) => pos)

  implicit def valueTobinder[A, D <: DriverConf](a: A)(implicit vb: ValueBinder[A, D]): Binder[D] = vb.apply(a)

  implicit def tuple2Binder[A, B, D <: DriverConf](implicit ab: ValueBinder[A, D],
                                                   bb: ValueBinder[B, D]): ValueBinder[(A, B), D] =
    ValueBinder[Tuple2[A, B], D]((a, ps, pos) => {
      val next = ab(a._1).bind(ps, pos)
      bb(a._2).bind(ps, next)
    })

  implicit def parameterListBinder[A, D <: DriverConf](
    implicit b: ValueBinder[A, D]
  ): ValueBinder[ParameterList[A], D] =
    ValueBinder((items, ps, pos) => {
      items.foldLeft(pos) {
        case (index, item) => b(item).bind(ps, index)
      }
    })
}
