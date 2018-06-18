package io.basico.driver

import io.basico.driver.Query.ReturnKind
import org.reactivestreams.Publisher

/**
  * @author Andrei Tupitcyn
  */
trait QueryExecutor[D <: DriverConf] {
  def execute(query: Query[D]): Publisher[Row[D]]
  def update(query: Query[D]): Int
  def updateAndReturn[A](query: Query[D], returnKind: ReturnKind, rowReader: RowReader[A, D]): A
}
