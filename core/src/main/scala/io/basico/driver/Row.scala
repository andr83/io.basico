package io.basico.driver

/**
  * Result row instance retrieved via `QueryExecutor`.
  *
  * @author Andrei Tupitcyn
  */
trait Row[D <: DriverConf] {
  // get value by type `A` at column index `index`
  def get[A](index: Int)(implicit reader: ColumnReader[A, D]): A
}
