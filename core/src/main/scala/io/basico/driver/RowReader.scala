package io.basico.driver

/**
  * @author Andrei Tupitcyn
  */
trait RowReader[R, D <: DriverConf] {
  def build(rs: D#ResultSet): () => R
}

object RowReader {
  implicit def rowReaderFromColumnReader[A, D <: DriverConf](
    implicit columnReader: ColumnReader[A, D]
  ): RowReader[A, D] =
    new RowReader[A, D] {
      override def build(rs: D#ResultSet): () => A = {
        val col0 = columnReader.build(rs, 0)
        () => col0()
      }
    }

  implicit def tuple2RowReader[A, B, D <: DriverConf](implicit columnAReader: ColumnReader[A, D],
                                                      columnBReader: ColumnReader[B, D]): RowReader[(A, B), D] =
    new RowReader[(A, B), D] {
      override def build(rs: D#ResultSet): () => (A, B) = {
        val col0 = columnAReader.build(rs, 0)
        val col1 = columnBReader.build(rs, 1)
        () =>
          (col0(), col1())
      }
    }
}
