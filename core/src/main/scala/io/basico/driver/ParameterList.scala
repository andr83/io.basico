package io.basico.driver

/**
  * Non empty parameters list
  * @author Andrei Tupitcyn
  */
case class ParameterList[A](head: A, tail: Seq[A]) {
  def foldLeft[B](z: B)(op: (B, A) => B): B = {
    val acc = op(z, head)
    tail.foldLeft(acc)(op)
  }
}
