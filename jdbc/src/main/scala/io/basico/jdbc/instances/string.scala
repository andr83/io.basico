package io.basico.jdbc.instances

import java.sql.Types

import io.basico.driver.ValueBinder
import io.basico.jdbc.{JdbcColumnReader, JdbcDriver, TypeMeta}

/**
  * @author Andrei Tupitcyn
  */
trait JdbcStringInstances {
  implicit val jdbcStringTypeMeta: TypeMeta[String] = TypeMeta(Types.VARCHAR)

  implicit val jdbcStringBinder: ValueBinder[String, JdbcDriver] =
    ValueBinder((a, ps, index) => {
      ps.setString(index, a)
      index + 1
    })

  implicit val jdbcStringColumnReader: JdbcColumnReader[String] =
    JdbcColumnReader((rs, idx) => rs.getString(idx + 1))((rs, name) => rs.getString(name))
}

object string extends JdbcStringInstances
