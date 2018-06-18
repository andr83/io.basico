package io.basico.jdbc.syntax

import io.basico.jdbc.JdbcDriver
import io.basico.syntax.SqlInterpolator

/**
  * @author Andrei Tupitcyn
  */
trait ToSqlInterpolator {
  implicit def toSqlInterpolator(sc: StringContext): SqlInterpolator[JdbcDriver] = new SqlInterpolator(sc)
}

object string extends ToSqlInterpolator
