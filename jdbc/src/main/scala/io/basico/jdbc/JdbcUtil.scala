package io.basico.jdbc

import scala.reflect.ClassTag

/**
  * @author Andrei Tupitcyn
  */
object JdbcUtil {
  def getArray[A : ClassTag](value: Any): Array[A] = {
    value match {
      case objArr: Array[A] @unchecked => objArr
      case _ =>
        val arrlength = java.lang.reflect.Array.getLength(value)
        (0 until arrlength).map(i => java.lang.reflect.Array.get(value, i).asInstanceOf[A]).toArray
    }
  }
}
