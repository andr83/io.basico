package io.basico

import driver.{ColumnReader, RowReader, Row, Query}
import io.DbIO

/**
  * @author Andrei Tupitcyn
  */
package object jdbc {
  type JdbcRowReader[R] = RowReader[R, JdbcDriver]
  type JdbcColumnReader[C] = ColumnReader[C, JdbcDriver]
  type JdbcIO[A] = DbIO[A, JdbcDriver]
  type JdbcRow = Row[JdbcDriver]
  type JdbcQuery = Query[JdbcDriver]
}
