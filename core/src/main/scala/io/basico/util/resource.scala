package io.basico.util

import scala.util.control.NonFatal

/**
  * @author Andrei Tupitcyn
  */
object resource {
  def withResource[A, R <: AutoCloseable](r: => R)(f: R => A): A = {
    val resource: R = r
    require(resource != null, "resource is null")
    var exception: Throwable = null
    try {
      f(resource)
    } catch {
      case e: Throwable =>
        exception = e
        throw e
    } finally {
      closeAndAddSuppressed(exception, resource)
    }
  }

  private def closeAndAddSuppressed(e: Throwable, resource: AutoCloseable): Unit = {
    if (e != null) {
      try {
        resource.close()
      } catch {
        case NonFatal(suppressed) =>
          e.addSuppressed(suppressed)
        case fatal: Throwable if NonFatal(e) =>
          fatal.addSuppressed(e)
          throw fatal
        case fatal: InterruptedException =>
          fatal.addSuppressed(e)
          throw fatal
        case fatal: Throwable =>
          e.addSuppressed(fatal)
      }
    } else {
      resource.close()
    }
  }

}
