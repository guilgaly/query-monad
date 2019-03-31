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

package com.zengularity.querymonad.test.module.sql.utils

import java.sql.Connection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import acolyte.jdbc.{
  AcolyteDSL,
  QueryResult => AcolyteQueryResult,
  ScalaCompositeHandler
}

import com.zengularity.querymonad.module.sql.WithSqlConnection

object SqlConnectionFactory {

  def withSqlConnection[A <: AcolyteQueryResult](
      resultsSet: A
  ): WithSqlConnection =
    new WithSqlConnection {
      def apply[B](f: Connection => Future[B]): Future[B] =
        AcolyteDSL.withQueryResult(resultsSet) { connection =>
          f(connection).andThen { case _ => connection.close() }
        }

    }

  def withSqlConnection(handler: ScalaCompositeHandler): WithSqlConnection =
    new WithSqlConnection {
      def apply[B](f: Connection => Future[B]): Future[B] = {
        val con = AcolyteDSL.connection(handler)
        f(con).andThen { case _ => con.close() }
      }
    }

}
