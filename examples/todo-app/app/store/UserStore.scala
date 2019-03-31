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

package com.zengularity.querymonad.examples.todoapp.store

import java.util.UUID

import anorm._

import com.zengularity.querymonad.examples.todoapp.model.User
import com.zengularity.querymonad.module.sql.SqlQuery

class UserStore() {

  def createUser(user: User): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"INSERT INTO users values (${user.id}, ${user.login}, ${user.fullName})"
        .executeInsert(SqlParser.scalar[String].singleOpt)
    }.map(_ => ())

  def getUser(userId: UUID): SqlQuery[Option[User]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM users WHERE id = $userId".as(User.parser.singleOpt)
    }

  def getByLogin(login: String): SqlQuery[Option[User]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM users WHERE login = $login".as(User.parser.singleOpt)
    }

  def deleteUser(userId: UUID): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"DELETE FROM users WHERE id = $userId".execute()
    }.map(_ => ())

}
