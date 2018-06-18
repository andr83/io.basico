package io.basico.driver

/**
  * Read value of type `A` from driver `ResultSet` instance.
  *
  * @author Andrei Tupitcyn
  */
trait ColumnReader[A, D <: DriverConf] {
  def read(rs: D#ResultSet, index: Int): A
  def read(rs: D#ResultSet, name: String): A
}
