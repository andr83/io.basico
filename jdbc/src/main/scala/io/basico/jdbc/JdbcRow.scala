package io.basico.jdbc

import java.sql.ResultSet

/**
  * @author Andrei Tupitcyn
  */
object JdbcRow {
  def apply(rs: ResultSet): JdbcRow = new JdbcRow {
    override def get[AA](pos: Int)(implicit reader: JdbcColumnReader[AA]): AA = reader.read(rs, pos)
  }
}
