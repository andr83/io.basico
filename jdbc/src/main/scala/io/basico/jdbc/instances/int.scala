package io.basico.jdbc.instances

import java.sql.Types

import io.basico.driver.ValueBinder
import io.basico.jdbc.{JdbcColumnReader, JdbcDriver, TypeMeta}

/**
  * @author Andrei Tupitcyn
  */
trait JdbcIntInstances {
  implicit val jdbcIntTypeMeta: TypeMeta[Int] = TypeMeta(Types.INTEGER)

  implicit val jdbcIntBinder: ValueBinder[Int, JdbcDriver] = ValueBinder[Int, JdbcDriver]((a, ps, index) => {
    ps.setInt(index, a)
    index + 1
  })

  implicit val jdbcIntColumnReader: JdbcColumnReader[Int] =
    JdbcColumnReader[Int]((rs, idx) => rs.getInt(idx + 1))((rs, name) => rs.getInt(name))
}

object int extends JdbcIntInstances
