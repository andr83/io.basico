package io.basico.driver

/**
  * Bind a parameter by index to concrete driver binder implementation.
  * For JDBC it will `PreparedStatement` for example.
  *
  * @author Andrei Tupitcyn
  */
trait Binder[D <: DriverConf] {
  def bind(ps: D#ParameterBinder, pos: Int): Int
}
