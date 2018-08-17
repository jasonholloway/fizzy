package com.woodpigeon.Fizzy

import cats.effect.IO
import fs2.text
import java.nio.file.{ Path, Paths }
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest._
import fs2.{io,Stream}
import cats.effect._

class FizzySpec extends FlatSpec {

  it should "load ndjson rows" in {

    val r = io.file.readAllAsync[IO](Paths.get(getClass.getResource("/data.ndjson").getPath), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .flatMap { l =>
        Stream.eval(IO.pure(l))
      }
      .compile.toVector
      .unsafeRunSync()

    println(r)
  }


}
