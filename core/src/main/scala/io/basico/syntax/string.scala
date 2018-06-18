package io.basico.syntax

import io.basico.driver
import io.basico.driver.{DriverConf, Query, QueryExecutor, ValueBinder}
import io.basico.macros.TupleMacros

import scala.language.experimental.macros

/**
  * @author Andrei Tupitcyn
  */
class SqlInterpolator[D <: DriverConf](private val sc: StringContext) {
  object sql {
    def apply(args: Any*): Query[D] = macro TupleMacros.forwardImplStr

    def applyTuple(implicit aBinder: ValueBinder[Unit, D]): Query[D] = {
      new Query[D] {
        override type A = Unit

        override def holder: Query.ParameterHolder[A, D] = Query.ParameterHolder[A, D]((), aBinder)

        override def sql: String = sc.parts.mkString("")
      }
    }

    def applyTuple[AA](aa: AA)(implicit aBinder: ValueBinder[AA, D]): Query[D] = {
      new Query[D] {
        override type A = AA

        override def holder = new driver.Query.ParameterHolder[A, D](aa, aBinder)

        override def sql: String = sc.parts.mkString("", "?", "")
      }
    }
  }
}

trait ToSqlInterpolator {
  implicit def toSqlInterpolator[D <: DriverConf](sc: StringContext): SqlInterpolator[D] = {
    new SqlInterpolator(sc)
  }
}

object string extends ToSqlInterpolator
