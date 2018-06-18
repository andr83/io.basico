package io.basico.driver

/**
  * @author Andrei Tupitcyn
  */
trait Fragments[D <: DriverConf] {
  def in[AA](q: Query[D], head: AA, tail: AA*)(implicit binder: ValueBinder[ParameterList[AA], D]): Query[D] = {
    val a = head +: tail
    new Query[D] {
      override type A = (q.A, ParameterList[AA])

      override def holder: Query.ParameterHolder[(q.A, ParameterList[AA]), D] =
        q.holder ++ Query.ParameterHolder(ParameterList(head, tail), binder)

      override def sql: String = q.sql + " IN " + a.map(_ => "?").mkString("(", ",", ")")
    }
  }

  def in[AA](sql: String, head: AA, tail: AA*)(implicit binder: ValueBinder[ParameterList[AA], D]): Query[D] =
    in(Query[Unit, D](sql, ()), head, tail: _*)(binder)

  def inSeq[A](q: Query[D], values: Seq[A])(implicit binder: ValueBinder[ParameterList[A], D]): Query[D] =
    in[A](q, values.head, values.tail: _*)

  def inSeq[A](sql: String, values: Seq[A])(implicit binder: ValueBinder[ParameterList[A], D]): Query[D] =
    in[A](sql, values.head, values.tail: _*)
}
