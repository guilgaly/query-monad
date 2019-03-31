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

package com.zengularity.querymonad.core.database

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/**
 * Heavily inspired from work done by @cchantep in Acolyte (see acolyte.reactivemongo.ComposeWithCompletion)
 */
trait ComposeWithCompletion[F[_], Out] {
  type Outer

  def apply[In](loaner: WithResource[In], f: In => F[Out]): Future[Outer]
}

object ComposeWithCompletion extends LowPriorityCompose {

  type Aux[F[_], A, B] = ComposeWithCompletion[F, A] { type Outer = B }

  implicit def futureOut[A]: Aux[Future, A, A] =
    new ComposeWithCompletion[Future, A] {
      type Outer = A

      def apply[In](
          loaner: WithResource[In],
          f: In => Future[A]
      ): Future[Outer] = loaner(f)

      override val toString = "futureOut"
    }

}

trait LowPriorityCompose { _: ComposeWithCompletion.type =>

  implicit def pureOut[F[_], A](
      implicit ec: ExecutionContext
  ): Aux[F, A, F[A]] =
    new ComposeWithCompletion[F, A] {
      type Outer = F[A]

      def apply[In](loaner: WithResource[In], f: In => F[A]): Future[Outer] =
        loaner(r => Future(f(r)))

      override val toString = "pureOut"
    }

}
