package io.basico.jdbc

import java.sql.ResultSet

/**
  * @author Andrei Tupitcyn
  */
object JdbcColumnReader {
  def apply[A](readerByIndex: (ResultSet, Int) => A)(readerByName: (ResultSet, String) => A): JdbcColumnReader[A] =
    new JdbcColumnReader[A] {
      //@inline override def read(rs: ResultSet, index: Int): A = readerByIndex(rs, index)
      //@inline override def read(rs: ResultSet, name: String): A = readerByName(rs, name)
      override def build(rs: ResultSet, colIndex: Int): () => A = () => readerByIndex(rs, colIndex)
    }
}
