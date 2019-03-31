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

package com.zengularity.querymonad.test.module.sql.models

import anorm._
import acolyte.jdbc.Implicits._
import acolyte.jdbc.{QueryResult => AcolyteQueryResult}
import acolyte.jdbc.RowLists.rowList4

import com.zengularity.querymonad.module.sql.SqlQuery

case class Professor(id: Int, name: String, age: Int, material: Int)

object Professor {
  val schema = rowList4(
    classOf[Int]    -> "id",
    classOf[String] -> "name",
    classOf[Int]    -> "age",
    classOf[Int]    -> "material"
  )

  val parser = Macro.namedParser[Professor]

  val resultSet: AcolyteQueryResult =
    Professor.schema :+ (1, "John Doe", 35, 1)

  def fetchProfessor(id: Int): SqlQuery[Option[Professor]] =
    SqlQuery { implicit connection =>
      SQL"SELECT * FROM professors where id = $id"
        .as(Professor.parser.singleOpt)
    }
}
