package io.basico

import io.basico.driver.{ColumnReader, Query, RowReader}
import io.basico.io.DbIO

/**
  * @author Andrei Tupitcyn
  */
package object jdbc {
  type JdbcRowReader[R] = RowReader[R, JdbcDriver]
  type JdbcColumnReader[C] = ColumnReader[C, JdbcDriver]
  type JdbcIO[A] = DbIO[A, JdbcDriver]
  //type JdbcRow = Row[JdbcDriver]
  type JdbcQuery = Query[JdbcDriver]
}
