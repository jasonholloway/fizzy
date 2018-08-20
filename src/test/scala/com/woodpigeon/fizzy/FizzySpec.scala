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
  def load[F[_]](name: String): Stream[F, String]
  def save[F[_]](name: String)(implicit E: Effect[F]): Sink[F, String]
}


class FileStore(root: Path) extends Store {

  def load[F[_]](name: String): Stream[F, String] = ???

  def save[F[_]](name: String)(implicit E: Effect[F]): Sink[F, String] =
    _.through(text.utf8Encode)
      .to(writeAllAsync(Paths.get(root.toString(), s"$name.ndjson")))
}


class FileStoreSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  val dataDir = tmp.dir(deleteOnExit = true)
  println(s"dataDir: $dataDir")

  val store = new FileStore(dataDir)

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




  def createLines(): Stream[IO, String] =
    Stream.emit("{ \"\"hello!\"\": 13 }").
      repeat.take(10)

  def log[F[_]: Effect, V](implicit L: LiftIO[F]): Pipe[F, V, V] = 
    _.evalMap(v => {
      L.liftIO(IO { println(v); v });
    })
}
