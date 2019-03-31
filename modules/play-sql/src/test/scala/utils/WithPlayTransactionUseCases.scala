/*
 * Copyright (c) 2018 Zengularity SA (FaberNovel Technologies) <https://www.zengularity.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.zengularity.querymonad.test.module.playsql.utils

import java.sql.SQLException
import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.ExecutionContext

import acolyte.jdbc.{AcolyteDSL, UpdateExecution}
import acolyte.jdbc.Implicits._
import acolyte.jdbc.play.AcolyteDatabase
import acolyte.jdbc.RowLists.{rowList1, rowList2}
import org.specs2.execute.Result
import play.api.db.Database

import com.zengularity.querymonad.module.sql.{SqlQueryRunner, WithSqlConnection}
import com.zengularity.querymonad.module.playsql.database.WithPlayTransaction

object WithPlayTransactionUseCases {

  private def testRunner[A](in: A)(f: A => Result) = f(in)

  val test = testRunner[SqlQueryRunner] _

  def test1 = testRunner[(SqlQueryRunner, AtomicReference[Int])] _

  def useCase1(implicit ec: ExecutionContext): SqlQueryRunner = {
    val queryResult = rowList1(
      classOf[String] -> "author_name"
    ) :+ ("Martin Odersky")
    val database: Database =
      new AcolyteDatabase(AcolyteDSL.handleQuery(_ ⇒ queryResult))
    val withSqlConnection: WithSqlConnection =
      new WithPlayTransaction(database)
    SqlQueryRunner(withSqlConnection)
  }

  def useCase2(implicit ec: ExecutionContext): SqlQueryRunner = {
    val queryResult =
      rowList2(
        classOf[String] -> "author_name",
        classOf[String] -> "book_name"
      ) :+ ("Martin Odersky", "Programming in Scala")
    val database: Database =
      new AcolyteDatabase(AcolyteDSL.handleQuery(_ ⇒ queryResult))
    val withSqlConnection: WithSqlConnection =
      new WithPlayTransaction(database)
    SqlQueryRunner(withSqlConnection)
  }

  def useCase3(
      implicit ec: ExecutionContext
  ): (SqlQueryRunner, AtomicReference[Int]) = {
    val step: AtomicReference[Int] = new AtomicReference(0)
    val handler = AcolyteDSL.handleStatement
      .withUpdateHandler {
        case UpdateExecution("insert into author values (2, 'Sam Haliday')",
                             Nil) =>
          val _ = step.compareAndSet(0, 1)
          1
        case UpdateExecution(
            "insert into book values (2, 'Functional programming in Scala for mortals', 2018, 'Packt', 2)",
            Nil
            ) =>
          val _ = step.compareAndSet(1, 2)
          1
        case u => throw new SQLException(s"Unexpected updated: $u")
      }
    val resHandler: acolyte.jdbc.ResourceHandler =
      AcolyteDSL.handleTransaction(whenCommit = { _ =>
        val _ = step.compareAndSet(2, 3)
      })
    val database: Database = new AcolyteDatabase(handler, resHandler)
    val withSqlConnection: WithSqlConnection =
      new WithPlayTransaction(database)
    (SqlQueryRunner(withSqlConnection), step)
  }

  def useCase4(
      implicit ec: ExecutionContext
  ): (SqlQueryRunner, AtomicReference[Int]) = {
    val step: AtomicReference[Int] = new AtomicReference(0)
    val handler = AcolyteDSL.handleStatement
      .withUpdateHandler {
        case UpdateExecution("insert into fake_table values (2, 100, 2)",
                             Nil) =>
          val _ = step.compareAndSet(0, 1)
          throw new SQLException(
            "ERROR: relation \"fake_table\" does not exist"
          )
        case u => throw new SQLException(s"Unexpected update: $u")
      }
    val resHandler = AcolyteDSL.handleTransaction(whenRollback = { _ =>
      val _ = step.compareAndSet(1, 2)
    })
    val database: Database = new AcolyteDatabase(handler, resHandler)
    val withSqlConnection: WithSqlConnection =
      new WithPlayTransaction(database)
    (SqlQueryRunner(withSqlConnection), step)
  }

  def useCase5(
      implicit ec: ExecutionContext
  ): (SqlQueryRunner, AtomicReference[Int]) = {
    val step: AtomicReference[Int] = new AtomicReference(0)
    val handler = AcolyteDSL.handleStatement
      .withUpdateHandler {
        case UpdateExecution("insert into author values (3, 'Josh Suereth')",
                             Nil) =>
          step.compareAndSet(0, 1)
          1
        case UpdateExecution(
            "insert into boookk values (3, 'Sbt in action', 2014, 'O''Reilly', 3)",
            Nil
            ) =>
          val _ = step.compareAndSet(1, 2)
          throw new SQLException("ERROR: relation \"boookk\" does not exist")
        case u => throw new SQLException(s"Unexpected update: $u")
      }
    val resHandler = AcolyteDSL.handleTransaction(whenRollback = { _ =>
      val _ = step.compareAndSet(2, 3)
    })
    val database: Database = new AcolyteDatabase(handler, resHandler)
    val withSqlConnection: WithSqlConnection =
      new WithPlayTransaction(database)
    (SqlQueryRunner(withSqlConnection), step)
  }

}
