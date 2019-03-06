package xyz.sigmalab.fpwebkit.demo

import doobie._
import doobie.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.io._
import cats._
import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import org.http4s.multipart.Multipart

trait FileUpload {

    // https://stackoverflow.com/questions/47368919/processing-multipart-content-in-http4s

    val fileUploadRoute = HttpRoutes.of[IO] {
        case req @ POST -> Root / "post" => {
            req.decode[Multipart[IO]] { m => {
                m.parts.find(_.name == Some("dataFile")) match {
                    case None => BadRequest(s"Not file")
                    case Some(part) => for {
                        // contents <- part.body.through(fs2.text.utf8Decode).runFoldMonoid
                        // https://github.com/functional-streams-for-scala/fs2/issues/1017#issuecomment-351708103
                        contents <- part.body.through(fs2.text.utf8Decode).compile.foldMonoid
                        response <- Ok(
                            s"""Multipart Data\nParts:${m.parts.length}
                                 |File contents: ${contents}""".stripMargin)
                    } yield response
                }
            }}
        }
    }

}
