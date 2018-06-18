package io.basico.jdbc.syntax

import java.sql.Connection

import io.basico.jdbc.JdbcQueryExecutor
import javax.sql.DataSource

import scala.util.control.NonFatal

/**
  * @author Andrei Tupitcyn
  */
final class DataSourceOps(val dataSource: DataSource) extends AnyVal {
  def withQueryExecutor[A](f: JdbcQueryExecutor => A): A = {
    var conn: Connection = null
    var ex: Throwable = null
    try {
      conn = dataSource.getConnection
      val qe = new JdbcQueryExecutor(conn)
      f(qe)
    } catch {
      case NonFatal(e) =>
        ex = e
        throw e
    } finally {
      if (ex != null) {
        try {
          conn.close()
        } catch {
          case NonFatal(s) =>
            ex.addSuppressed(s)
        }
      } else {
        conn.close()
      }
    }
  }
}

trait DataSourceSyntax {
  final implicit def dataSourceSyntaxOps(dataSource: DataSource): DataSourceOps = new DataSourceOps(dataSource)
}

object dataSource extends DataSourceSyntax
