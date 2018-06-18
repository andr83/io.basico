package io.basico.driver

/**
  * Concrete database driver configuration describes some of it's internal implementations:
  *
  * `ResultSet` what driver's result type is
  * `ParameterBinder` driver's parameter binder type
  *
  * @author Andrei Tupitcyn
  */
trait DriverConf {
  type ResultSet
  type ParameterBinder
}
