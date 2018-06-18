package io.basico.jdbc.syntax

/**
  * @author Andrei Tupitcyn
  */
trait AllSyntax extends ToSqlInterpolator with io.basico.syntax.ToQueryOps

object all extends AllSyntax
