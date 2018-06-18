package io.basico.syntax

import io.basico.driver.{DriverConf, Query, ResultReader, ValueBinder}
import io.basico.io.DbIO.{QueryIO, UpdateIO}
import io.basico.macros.TupleMacros

import scala.language.{higherKinds, implicitConversions}
import scala.languageFeature.experimental.macros

/**
  * @author Andrei Tupitcyn
  */
class QueryOps[D <: DriverConf](query: Query[D]) {
  def as[A](implicit resultReader: ResultReader[A, D]): QueryIO[A, D] = QueryIO(query, resultReader)
  def update: UpdateIO[D] = UpdateIO(query)

  object bind {
    def apply(args: Any*): Query[D] = macro TupleMacros.forwardImplStr

    def applyTuple(): Query[D] = query

    def applyTuple[AA](aa: AA)(implicit aBinder: ValueBinder[AA, D]): Query[D] = {
      new Query[D] {
        override type A = (AA, query.A)

        override def holder: Query.ParameterHolder[A, D] =
          Query.ParameterHolder[A, D](value = (aa, query.holder.value), binder = aBinder ++ query.holder.binder)

        override def sql: String = query.sql
      }
    }
  }
}

trait ToQueryOps {
  implicit def toQueryOps[D <: DriverConf](query: Query[D]): QueryOps[D] =
    new QueryOps[D](query)
}

object query extends ToQueryOps
