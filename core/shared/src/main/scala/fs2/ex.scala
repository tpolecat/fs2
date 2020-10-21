/*
 * Copyright (c) 2013 Functional Streams for Scala
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

package fs2


object ex {

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._

val emitThenWait = Stream("elem") ++ Stream.sleep_[IO](200.millis)
val s = emitThenWait.repeat.take(3)

def a = s.pull
 .timed { timedPull =>
    def go(timedPull: Stream.TimedPull[IO, String]): Pull[IO, String, Unit] =
      timedPull.timeout(150.millis) >> // starts a timeout timer, resets the previous one
      timedPull.uncons.flatMap {
        case Some((Right(elems), next)) => Pull.output(elems) >> go(next)
        case Some((Left(_), next)) => Pull.output1("late!") >> go(next)
        case None => Pull.done
      }

      go(timedPull)
 }.stream.compile.toVector.unsafeRunSync()
//val res0: Vector[String] = Vector(elem, late!, elem, late!, elem)
}
