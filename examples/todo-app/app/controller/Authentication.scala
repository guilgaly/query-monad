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

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import play.api.mvc._

trait Authentication { self: BaseController =>

  implicit def ec: ExecutionContext

  case class ConnectedUserInfo(
      id: UUID,
      login: String
  )

  case class ConnectedUserRequest[A](
      userInfo: ConnectedUserInfo,
      request: Request[A]
  ) extends WrappedRequest[A](request)

  def ConnectedAction = Action andThen ConnectionRefiner

  private def ConnectionRefiner =
    new ActionRefiner[Request, ConnectedUserRequest] {
      def executionContext = ec
      def refine[A](request: Request[A]) =
        Future.successful(
          request.session
            .get("id")
            .flatMap(str => Try(UUID.fromString(str)).toOption)
            .zip(request.session.get("login"))
            .headOption
            .map {
              case (id, login) =>
                ConnectedUserRequest(ConnectedUserInfo(id, login), request)
            }
            .toRight(Unauthorized("Missing credentials"))
        )
    }

}
