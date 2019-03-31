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

package com.zengularity.querymonad.examples.todoapp.wiring

import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.routing.Router
import router.Routes

import com.zengularity.querymonad.module.sql.{SqlQueryRunner}
import com.zengularity.querymonad.module.playsql.database.WithPlayTransaction
import com.zengularity.querymonad.examples.todoapp.controller.{
  TodoController,
  UserController
}
import com.zengularity.querymonad.examples.todoapp.store.{
  CredentialStore,
  TodoStore,
  UserStore
}

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with EvolutionsComponents
    with HikariCPComponents
    with NoHttpFiltersComponents {

  val db = dbApi.database("default")

  val queryRunner = SqlQueryRunner(new WithPlayTransaction(db))

  // Stores
  val userStore: UserStore = new UserStore()
  val todoStore: TodoStore = new TodoStore()
  val credentialStore: CredentialStore = new CredentialStore()

  val router: Router = new Routes(
    httpErrorHandler,
    new UserController(queryRunner,
                       userStore,
                       credentialStore,
                       controllerComponents),
    new TodoController(queryRunner, todoStore, userStore, controllerComponents)
  )

  applicationEvolutions.start()
}

class AppLoader extends ApplicationLoader {
  def load(context: Context) = new AppComponents(context).application
}
