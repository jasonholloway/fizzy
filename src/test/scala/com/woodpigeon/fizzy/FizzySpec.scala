package com.woodpigeon.Fizzy

import cats.effect.{ IO, Sync }
import cats.effect.IO._
import fs2.{ Pipe, Sink, text }
import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest._
import fs2.{io,Stream}
import fs2.io.file._
import cats.effect._
import ammonite.ops._
import cats.instances.string._


trait Store {
  def load[F[_]](name: String)(implicit E: Effect[F]): Stream[F, String]
  def save[F[_]](name: String)(implicit E: Effect[F]): Sink[F, String]
}


class FileStore(root: Path) extends Store {

  def formPath(name: String): java.nio.file.Path =
    Paths.get((root / s"$name.ndjson").toString)

  def load[F[_]](name: String)(implicit E: Effect[F]): Stream[F, String] =
    readAllAsync[F](formPath(name), 256)
      .through(text.utf8Decode)
      .through(text.lines)

  def save[F[_]](name: String)(implicit E: Effect[F]): Sink[F, String] =
    _.map(l => s"$l\n")
      .through(text.utf8Encode)
      .to(writeAllAsync(formPath(name)))
}


class FileStoreSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  import Helpers._

  val dataDir = tmp.dir(deleteOnExit = true)
  val store = new FileStore(dataDir)

  println(s"dataDir: $dataDir")

  override def beforeAll() = 
    createLines()
      .through(log)
      .to(store.save("blaaah"))
      .run.unsafeRunSync()

  it should "create named file" in {
    val files = ls! dataDir
    files should contain (dataDir / "blaaah.ndjson")
  }

  it should "fill file with requisite number of lines" in {
    val lines = read.lines! dataDir / "blaaah.ndjson"
    assert(lines.length == 10)
  }



  it should "load lines from file" in {
    write(dataDir / "t.ndjson", createLines().intersperse("\n").sync)

    val r = store.load("t").sync
    assert(r.length == 10)
  }

}


object Helpers {

  def createLines(): Stream[IO, String] =
    Stream.emit("{ \"\"hello!\"\": 13 }").
      repeat.take(10)

  def log[F[_]: Effect, V](implicit L: LiftIO[F]): Pipe[F, V, V] = 
    _.evalMap(v => {
      L.liftIO(IO { println(v); v });
    })
  
  implicit class StreamHelpers[V](str: Stream[IO, V]) {
    def sync(): List[V] =
      str.compile.toList.unsafeRunSync()
  }

}
