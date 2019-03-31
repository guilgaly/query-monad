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

case class Material(id: Int, name: String, numberOfHours: Int, level: String)

object Material {
  val schema = rowList4(
    classOf[Int]    -> "id",
    classOf[String] -> "name",
    classOf[Int]    -> "numberOfHours",
    classOf[String] -> "level"
  )

  val parser = Macro.namedParser[Material]

  val resultSet: AcolyteQueryResult =
    Material.schema :+ (1, "Computer Science", 20, "Beginner")

  def fetchMaterial(id: Int): SqlQuery[Option[Material]] =
    SqlQuery { implicit connection =>
      SQL"SELECT * FROM materials where id = $id"
        .as(Material.parser.singleOpt)
    }
}
