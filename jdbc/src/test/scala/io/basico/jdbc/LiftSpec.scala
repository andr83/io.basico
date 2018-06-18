package io.basico.jdbc

import org.scalatest.{FlatSpec, Matchers}
import io.basico.jdbc.implicits._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Andrei Tupitcyn
  */
class LiftSpec extends FlatSpec with Matchers with DbIOTest {
  "DbIO" should "lift to Future context" in {
    withQueryExecutor { implicit qe =>
      val future = sql"select int_field from test where int_field = 10".as[Int].liftF[Future]
      Await.result(future, 10.second) shouldBe 10
    }
  }
}
