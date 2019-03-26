package xyz.sigmalab.fptemplate.demov1.webapi

import java.nio.charset.StandardCharsets
import java.nio.file.{Path}
// import java.nio.file.{Paths, StandardCopyOption}
// import cats._
// import cats.implicits._
import cats.effect._
// import cats.effect.implicits._
import org.http4s._
import org.http4s.dsl.io._
// import org.http4s.implicits._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext

class HttpDebugRoute[F[_] : Effect](
  blockingEC: ExecutionContext,
  ctxShift: ContextShift[IO]
) {

  /* Compile Problem 
  object HttpDebugRoutesDef {
    val debugPost = POST -> Root / "debug"
    val debugGet = GET -> Root / "debug"
  }
  import HttpDebugRoutesDef._

  def anotherHttpDebugRoutes : HttpRoutes[F] = HttpRoutes.of[F] {
    case `debugPost` => ???
    case `debugGet` => ???
  } */

  def httpDebugRoutes : HttpRoutes[IO] = HttpRoutes.of[IO] {
        case req @ GET -> Root / "debug" =>
            val headers = req.headers.foldLeft(new StringBuilder(1024)) { case (buf, hdr) =>
                buf append f"${hdr.name.value }%20s : ${hdr.value}; (${hdr.toString})\n"
            }.toString
            Ok {
                f"""
                     | IsSecure: ${req.isSecure}
                     | REQ: ${req.method}
                     | URI: ${req.uri}
                     | QueryString: ${req.queryString}
                     | FROM: ${req.from}
                     | HEADERS:
                     | ${headers}
                """.stripMargin
            }

        case req @ POST -> Root / "debug" =>

            for {

                bytes <- req.body.compile.foldChunks(new ArrayBuffer[Byte]()) { (buf, chunk) =>
                    val iter = chunk.iterator
                    while ( iter.hasNext ) buf.append(iter.next)
                    buf
                }

                bodyText = new String(bytes.toArray, StandardCharsets.UTF_8)

                headers = req.headers.foldLeft(new StringBuilder(1024)) { case (buf, hdr) =>
                    buf append f"${hdr.name.value }%20s : ${hdr.value}; (${hdr.toString})\n"
                }.toString

                // done <- Ok(bodyText)
                done <- Ok {
                    f"""
                         | IsSecure: ${req.isSecure}
                         | REQ: ${req.method}
                         | URI: ${req.uri}
                         | QueryString: ${req.queryString}
                         | FROM: ${req.from}
                         | HEADERS:
                         | ${headers}
                         | Body:
                         | ${bodyText}
                    """.stripMargin
                }

            } yield done

    }

    def reqFileUpload(uri: Uri, keyName: String, filePath: Path): Request[IO] = {
        import org.http4s.multipart._
        import org.http4s.headers.`Content-Type`

        val fileRef = Part.fileData[IO](
            keyName,
            filePath.toFile,
            blockingEC,
            `Content-Type`(MediaType.text.plain)
        )(Sync[IO], ctxShift)

        val multipart = Multipart[IO](Vector(fileRef))

        Request[IO](Method.POST, uri)
            .withEntity(multipart)
            .withHeaders(multipart.headers)
    }

}
