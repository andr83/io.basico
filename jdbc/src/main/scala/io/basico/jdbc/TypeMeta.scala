package io.basico.jdbc

import java.sql.JDBCType

/**
  * @author Andrei Tupitcyn
  */
trait TypeMeta[A] {

  /**
    * Name of SQL type
    * see java.sql.Types
    */
  def sqlType: String

  /**
    * JDBC type
    * see java.sql.Types
    */
  def jdbcType: Int
}

object TypeMeta {
  def apply[A](jdbcTyp: Int): TypeMeta[A] = new TypeMeta[A] {

    /**
      * Name of SQL type
      * see java.sql.Types
      */
    override def sqlType: String = JDBCType.valueOf(jdbcType).getName

    /**
      * JDBC type
      * see java.sql.Types
      */
    override def jdbcType: Int = jdbcTyp
  }
}
