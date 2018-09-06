package io.basico.driver

import io.basico.driver.Query.ReturnKind
import org.reactivestreams.Publisher

/**
  * @author Andrei Tupitcyn
  */
trait QueryExecutor[D <: DriverConf] {
  def execute[A](query: Query[D], rowReader: RowReader[A, D]): Publisher[A]
  def update(query: Query[D]): Int
  def updateAndReturn[A](query: Query[D], returnKind: ReturnKind, rowReader: RowReader[A, D]): A
}
