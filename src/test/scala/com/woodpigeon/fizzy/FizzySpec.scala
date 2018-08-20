package com.woodpigeon.Fizzy

import cats.effect.{ IO, Sync }
import fs2.{ Pipe, Pure, text }
import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest._
import fs2.{io,Stream}
import fs2.io.file._
import cats.effect._
import ammonite.ops._


trait Store {
  def load[F[_]](name: String): Stream[F, String]
  def save[F[_]](name: String): Pipe[F, String, Nothing]
}


class FileStore(root: Path) extends Store {
  def load[F[_]](name: String): Stream[F, String] = ???

  def save[F[_]](name: String): Pipe[F, String, Byte] =
    _.through(text.utf8Encode)
      .through(writeAllAsync(Paths.get("")))
}


class FileStoreSpec extends FlatSpec with Matchers {

  val dataDir = tmp.dir(deleteOnExit = true)
  val store = new FileStore(dataDir)

  def createLines(): Stream[IO, String] =
    Stream.emit("{ \"\"hello\"\": 13 }").
      repeat.take(10)

  it should "save to data folder" in {
    createLines()
      .through(store.save("blaaah"))
      .run

    val files = ls! dataDir
    files should contain (dataDir / "blaaah.ndjson")
  }

  ignore should "stream named data from the data folder" in {
    val lineCount = store.load[IO]("blah")
      .runFold(0)((c, _) => c + 1)
    
    assert(lineCount == 4)
  }
}


class FizzySpec extends FlatSpec with BeforeAndAfterAll with Matchers {


  ignore should "load ndjson rows" in {
    ???
    // val r = io.file.readAllAsync[IO](Paths.get(getClass.getResource("/data.ndjson").getPath), 4096)
    //   .through(text.utf8Decode)
    //   .through(text.lines)
    //   .flatMap { l =>
    //     Stream.eval(IO { 13 })
    //   }
    //   .compile.toList
    //   .unsafeRunSync()

    // println(r)
  }



}

//
//
//
//
//
//
//
//

