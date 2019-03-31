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

package com.zengularity.querymonad.module.playsql.database

import java.sql.Connection

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import com.typesafe.scalalogging.Logger
import play.api.db.Database

import com.zengularity.querymonad.module.sql.WithSqlConnection

class WithPlayTransaction(db: Database)(implicit ec: ExecutionContext)
    extends WithSqlConnection {

  val logger = Logger[WithPlayTransaction]

  def apply[A](f: Connection => Future[A]): Future[A] = {
    lazy val connection = db.getConnection(false)
    f(connection)
      .andThen {
        case Success(x) =>
          connection.commit()
          x
        case Failure(ex) =>
          logger.debug(
            s"an error occurred when runnning the operation on database: $ex"
          )
          connection.rollback()
      }
      .andThen { case _ => connection.close() }
  }

}
