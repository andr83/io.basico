package io.basico.syntax

/**
  * @author Andrei Tupitcyn
  */
trait AllSyntax extends ToQueryOps with ToSqlInterpolator

object all extends AllSyntax
