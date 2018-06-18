package io.basico.jdbc.instances

/**
  * @author Andrei Tupitcyn
  */
trait AllInstances
    extends JdbcStringInstances
    with JdbcIntInstances
    with JdbcTraversableInstances
    with JdbcRowReaderInstances

object all extends AllInstances
