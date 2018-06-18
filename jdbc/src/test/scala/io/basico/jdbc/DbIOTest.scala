package io.basico.jdbc

import java.sql.DriverManager
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

import io.basico.io.DbIO
import io.basico.jdbc.implicits._

/**
  * @author Andrei Tupitcyn
  */
trait DbIOTest {
  val instant: Instant = LocalDateTime
    .parse("2000-01-01 13:59:12", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    .atZone(ZoneId.systemDefault())
    .toInstant

  val today: LocalDate = LocalDate.now
  val yesterday: LocalDate = LocalDate.now().minusDays(1)

  def withQueryExecutor(test: JdbcQueryExecutor => Unit): Unit = {
    Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection("jdbc:h2:mem:test")
    implicit val qe: JdbcQueryExecutor = new JdbcQueryExecutor(conn)
    try {
      val stmt = conn.createStatement()
      stmt.executeUpdate("""
                           |CREATE TABLE test (
                           | id BIGINT auto_increment,
                           | int_field INTEGER,
                           | long_field BIGINT,
                           | bool_field BOOLEAN,
                           | time_field TIMESTAMP,
                           | date_field DATE,
                           | string_field VARCHAR(250),
                           | array_field ARRAY
                           |)
                         """.stripMargin)
      stmt.close()

      def insert(int: Int, string: String, array: Seq[Int]) = {
        sql"""
             |INSERT INTO test(int_field, string_field, array_field)
             |VALUES ($int, $string, $array)
           """.stripMargin.update
      }

      (for {
        _ <- insert(10, "Hello", Seq(1, 2, 3))
        _ <- insert(11, "World", List(4, 5, 6))
      } yield {
        test(qe)
      }).unsafeRunSync(qe)
    } finally {
      conn.close()
    }
  }

  def fromIO[A](test: => DbIO[A, JdbcDriver]): Unit = {
    withQueryExecutor { implicit qe =>
      test.unsafeRunSync
    }
  }
}
