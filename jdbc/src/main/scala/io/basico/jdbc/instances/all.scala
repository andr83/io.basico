package io.basico.jdbc.instances

/**
  * @author Andrei Tupitcyn
  */
trait AllInstances
    extends JdbcStringInstances
    with JdbcIntInstances
    with JdbcTraversableInstances

object all extends AllInstances
