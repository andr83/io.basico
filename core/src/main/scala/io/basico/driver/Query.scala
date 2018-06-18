package io.basico.driver

/**
  * @author Andrei Tupitcyn
  */
trait Query[D <: DriverConf] { self =>
  type A
  def holder: Query.ParameterHolder[A, D]
  def sql: String

  def merge(other: Query[D]): Query[D] = new Query[D] {
    override type A = (self.A, other.A)

    override def holder: Query.ParameterHolder[(self.A, other.A), D] = self.holder ++ other.holder

    override def sql: String = self.sql + other.sql
  }

  def ++(other: Query[D]): Query[D] = merge(other)

  def stripMargin(marginChar: Char): Query[D] = new Query[D] {
    type A = self.A
    def holder: Query.ParameterHolder[Query.this.A, D] = self.holder
    def sql: String = self.sql.stripMargin(marginChar)
  }

  def stripMargin: Query[D] = stripMargin('|')
}

object Query {
  def apply[AA, D <: DriverConf](_sql: String, a: AA)(implicit binder: ValueBinder[AA, D]): Query[D] = new Query[D] {
    override type A = AA

    override def holder: ParameterHolder[AA, D] = Query.ParameterHolder(a, binder)

    override def sql: String = _sql
  }

  sealed trait ReturnKind
  case object ReturnGeneratedKeys extends ReturnKind
  case object Returning extends ReturnKind

  case class ParameterHolder[A, D <: DriverConf](value: A, binder: ValueBinder[A, D]) {
    def merge[B](other: ParameterHolder[B, D]): ParameterHolder[(A, B), D] =
      new ParameterHolder[(A, B), D]((value, other.value), binder ++ other.binder)

    def ++[B](other: ParameterHolder[B, D]): ParameterHolder[(A, B), D] = merge(other)
  }
}
