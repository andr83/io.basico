package io.basico.jdbc.instances

import java.sql.Types

import io.basico.driver.{ColumnReader, ValueBinder}
import io.basico.jdbc.{JdbcColumnReader, JdbcDriver, JdbcUtil, TypeMeta}

import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag

/**
  * @author Andrei Tupitcyn
  */
trait JdbcTraversableInstances {
  def sqlArrayFromTraversableValueBinder[A: ClassTag, T <: Traversable[A]](
    implicit meta: TypeMeta[A]
  ): ValueBinder[T, JdbcDriver] =
    ValueBinder[T, JdbcDriver]((a, ps, index) => {
      if (a == null) {
        ps.setNull(index, Types.ARRAY)
        index + 1
      } else {
        val arr = ps.getConnection.createArrayOf(meta.sqlType, a.map(_.asInstanceOf[AnyRef]).toArray)
        ps.setArray(index, arr)
        index + 1
      }
    })

  implicit def jdbcSeqBinder[A: ClassTag](implicit meta: TypeMeta[A]): ValueBinder[Seq[A], JdbcDriver] =
    sqlArrayFromTraversableValueBinder[A, Seq[A]]

  implicit def jdbcArrayColumnReader[A: ClassTag]: JdbcColumnReader[Array[A]] =
    JdbcColumnReader((rs, idx) => {
      JdbcUtil.getArray[A](rs.getArray(idx + 1).getArray)
    })((rs, name) => {
      JdbcUtil.getArray[A](rs.getArray(name).getArray)
    })

  implicit def jdbcCBFColumnReader[C[_], A: ClassTag](
    implicit cbf: CanBuildFrom[C[A], A, C[A]]
  ): ColumnReader[C[A], JdbcDriver] = {
    JdbcColumnReader((rs, idx) => {
      val arr = jdbcArrayColumnReader[A].build(rs, idx)()
      val cb = cbf.apply()
      arr.foreach(cb.+=)
      cb.result()
    })((rs, name) => {
      val arr = jdbcArrayColumnReader[A].build(rs, 0)()
      val cb = cbf.apply()
      arr.foreach(cb.+=)
      cb.result()
    })
  }
}

object traversable extends JdbcTraversableInstances
