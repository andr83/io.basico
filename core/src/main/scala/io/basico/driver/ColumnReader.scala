package io.basico.driver

/**
  * Read value of type `A` from driver `ResultSet` instance.
  *
  * @author Andrei Tupitcyn
  */
trait ColumnReader[A, D <: DriverConf] {
  def build(rs: D#ResultSet, colIndex: Int): () => A
}
