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

package com.zengularity.querymonad.examples.todoapp.controller

import java.nio.charset.Charset
import java.util.{Base64, UUID}

import scala.concurrent.{ExecutionContext, Future}

import cats.instances.either._
import play.api.mvc._
import play.api.libs.json.Json

import com.zengularity.querymonad.examples.todoapp.controller.model.AddUserPayload
import com.zengularity.querymonad.examples.todoapp.model.{Credential, User}
import com.zengularity.querymonad.examples.todoapp.store.{
  CredentialStore,
  UserStore
}
import com.zengularity.querymonad.module.sql.{SqlQueryRunner, SqlQueryT}

class UserController(
    runner: SqlQueryRunner,
    store: UserStore,
    credentialStore: CredentialStore,
    cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractController(cc)
    with Authentication {

  type ErrorOrResult[A] = Either[String, A]

  def createUser: Action[AddUserPayload] =
    Action(parse.json[AddUserPayload]).async { implicit request =>
      val payload = request.body
      val query = for {
        _ <- SqlQueryT.fromQuery[ErrorOrResult, Unit](
          store.getByLogin(payload.login).map {
            case Some(_) => Left("User already exists")
            case None    => Right(())
          }
        )

        user = AddUserPayload.toModel(payload)(UUID.randomUUID())
        credential = AddUserPayload.toCredential(payload)

        _ <- SqlQueryT.liftQuery[ErrorOrResult, Unit](
          credentialStore.saveCredential(credential)
        )
        _ <- SqlQueryT.liftQuery[ErrorOrResult, Unit](store.createUser(user))
      } yield ()

      runner(query).map {
        case Right(_)          => NoContent
        case Left(description) => BadRequest(description)
      }
    }

  def getUser(userId: UUID): Action[AnyContent] = ConnectedAction.async {
    request =>
      if (request.userInfo.id == userId)
        runner(store.getUser(userId)).map {
          case Some(user) => Ok(Json.toJson(user))
          case None       => NotFound("The user doesn't exist")
        } else
        Future.successful(NotFound("Cannot operate this action"))
  }

  def deleteUser(userId: UUID): Action[AnyContent] = ConnectedAction.async {
    request =>
      val userInfo = request.userInfo
      if (userInfo.id == userId) {
        val query = for {
          _ <- credentialStore.deleteCredentials(userInfo.login)
          _ <- store.deleteUser(userId)
        } yield ()
        runner(query).map(_ => NoContent.withNewSession)
      } else
        Future.successful(BadRequest("Cannot operate this action"))
  }

  def login: Action[AnyContent] = Action.async { implicit request =>
    val authHeaderOpt = request.headers
      .get("Authorization")
      .map(_.substring("Basic".length()).trim())

    val query = for {
      credential <- SqlQueryT.liftF[ErrorOrResult, Credential](
        authHeaderOpt
          .map { encoded =>
            val decoded = Base64.getDecoder().decode(encoded)
            val authStr = new String(decoded, Charset.forName("UTF-8"))
            authStr.split(':').toList
          }
          .collect {
            case login :: password :: _ => Credential(login, password)
          }
          .toRight("Missing credentials")
      )

      exists <- SqlQueryT.liftQuery[ErrorOrResult, Boolean](
        credentialStore.check(credential)
      )

      user <- {
        if (exists)
          SqlQueryT.fromQuery[ErrorOrResult, User](
            store
              .getByLogin(credential.login)
              .map(_.toRight("The user doesn't exist"))
          )
        else
          SqlQueryT.liftF[ErrorOrResult, User](Left("Wrong credentials"))
      }
    } yield user

    runner(query).map {
      case Right(user) =>
        NoContent.withSession("id" -> user.id.toString, "login" -> user.login)
      case Left(description) => BadRequest(description).withNewSession
    }
  }

  def logout: Action[AnyContent] = ConnectedAction {
    NoContent.withNewSession
  }

}
