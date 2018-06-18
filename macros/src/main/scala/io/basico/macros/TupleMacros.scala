package io.basico.macros

import scala.reflect.macros.whitebox
import scala.language.dynamics
import scala.language.experimental.macros

/**
  * @author Andrei Tupitcyn
  */
class TupleMacros(val c: whitebox.Context) {
  import c.universe._

  def forwardImplStr(args: Tree*): Tree = {
    forward("apply", args)
  }

  def forwardImpl(method: Tree)(args: Tree*): Tree = {
    val q"${methodString: String}" = method
    forward(methodString, args)
  }

  def forward(method: String, args: Seq[Tree]): Tree = {
    val lhs = c.prefix.tree
    val lhsTpe = lhs.tpe

    val methodName = TermName(method + "Tuple")

    if (lhsTpe.member(methodName) == NoSymbol)
      c.abort(c.enclosingPosition, s"missing method '$methodName'")

    if (args.isEmpty) {
      q""" $lhs.$methodName(()) """
    } else {
      val argsTree = mkTupleImpl(args)
      q""" $lhs.$methodName($argsTree) """
    }
  }

  def mkTupleImpl(args: Seq[Tree]): Tree = {
    if (args.length == 1) {
      args.head
    } else {
      val elem = args.head
      val (neTpe, neTree) = (elem.tpe, elem)
      val tailElem = mkTupleImpl(args.tail)
      val (neTpe2, neTree2) = (tailElem.tpe, tailElem)
      q"""_root_.scala.Tuple2($neTree, $neTree2)"""
    }
  }
}
