package io.basico.driver

/**
  * @author Andrei Tupitcyn
  */
trait RowReader[R, D <: DriverConf] {
  def read(r: Row[D]): R
}

object RowReader {
  implicit def rowReaderFromColumnReader[A, D <: DriverConf](
    implicit columnReader: ColumnReader[A, D]
  ): RowReader[A, D] =
    new RowReader[A, D] {
      override def read(rs: Row[D]): A = rs.get[A](1)
    }

  implicit def tuple2RowReader[A, B, D <: DriverConf](implicit columnAReader: ColumnReader[A, D],
                                                      columnBReader: ColumnReader[B, D]): RowReader[(A, B), D] =
    new RowReader[(A, B), D] {
      override def read(rs: Row[D]): (A, B) = (rs.get[A](1), rs.get[B](2))
    }
}
