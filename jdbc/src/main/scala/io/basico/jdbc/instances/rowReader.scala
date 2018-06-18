package io.basico.jdbc.instances

import io.basico.driver.Row
import io.basico.jdbc.{JdbcColumnReader, JdbcDriver, JdbcRowReader}

/**
  * @author Andrei Tupitcyn
  */

trait JdbcRowReaderInstances {
  implicit def jdbcRowReaderFromColumnReader[A](implicit columnReader: JdbcColumnReader[A]): JdbcRowReader[A] =
    new JdbcRowReader[A] {
      override def read(rs: Row[JdbcDriver]): A = rs.get[A](1)
    }
}

object rowReader extends JdbcRowReaderInstances
