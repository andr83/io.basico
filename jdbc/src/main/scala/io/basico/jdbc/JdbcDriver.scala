package io.basico.jdbc

import java.sql.{PreparedStatement, ResultSet => JResultSet}

import io.basico.driver.DriverConf

/**
  * @author Andrei Tupitcyn
  */
class JdbcDriver extends DriverConf {
  override type ResultSet = JResultSet
  override type ParameterBinder = PreparedStatement
}
