package xyz.sigmalab.fptemplate.demov1.webapi

import java.nio.charset.StandardCharsets

import cats.effect._
import org.http4s._
// import io.circe._, io.circe.literal._, io.circe.parser._
import io.circe.generic.auto._, io.circe.syntax._
// import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.dsl.io._

import scala.collection.mutable.ArrayBuffer


import xyz.sigmalab.fptemplate.demov1.model.serv.TodoService

trait TodoRoute {

    // def todoTx: Resource[IO, doobie.Transactor[IO]]

    import TodoRoute._

    def todoService : TodoService

    def resourceTx : Resource[IO, doobie.Transactor[IO]]

    def todoRoute = HttpRoutes.of[IO] {

        case GET -> Root / "todo" / LongVar(org)
            :? QueryParam.TodoListLimit(limit)
            +& QueryParam.TodoListOffset(offset) =>

            val a = resourceTx.use { implicit tx =>
                for {
                    list <- todoService.list(org, Range.Long(offset, offset + limit, 1))
                } yield list.asJson
            }

            val b = Ok(a)

            /*
            val c = resourceTx.use { implicit tx => for {
                list <- todoService.list(org, Range.Long(offset, offset + limit, 1))
                result <- Ok(list.asJson)
            } yield result }
            */

            b

        case req @ POST -> Root / "debug" / "show_body" =>

            val finalVal = for {
                bytes <- req.body.compile.foldChunks(new ArrayBuffer[Byte]()) { (buf, chunk) =>
                    val iter = chunk.iterator
                    while ( iter.hasNext ) buf.append(iter.next)
                    buf
                }

                bodyText = new String(bytes.toArray, StandardCharsets.UTF_8)

                done <- Ok(bodyText)

            } yield done

            finalVal

        case req @ GET -> Root / "debug" / "return_json" =>
            import io.circe.literal._
            Ok(json"""{"name": "Reza", "age": 30}""")

        case req @ GET -> Root / "debug" / "return_json_v2" =>
            Ok(TodoRoute.UserDef("Reza", 30).asJson)

        case req @ POST -> Root / "debug" / "ops" =>
            // import io.circe.generic.auto._
            import org.http4s.circe._
            implicit val x = jsonOf[IO, TodoRoute.UserDef]
            for {
                user <- req.as[TodoRoute.UserDef]
                list = req.headers.map { h => f"${h.name.toString}%30s : ${h.value}" }
                done <- Ok(s"Hello: ${user.name} \n${list.mkString("\n")}")
            } yield done


        case req @ POST -> Root / "debug" / "file_up" =>

            // https://stackoverflow.com/questions/47368919/processing-multipart-content-in-http4s

            import org.http4s.multipart._
            import fs2.text.utf8Decode
            import cats.implicits._

            req.decode[Multipart[IO]] { m =>
                m.parts.find(_.name == Some("avatar")) match {
                    case None => BadRequest("OPS")
                    case Some(part) =>
                        for {
                            // contents <- part.body.through(utf8Decode).runFoldMonoid
                            contents <- part.body.through(utf8Decode).compile.foldMonoid
                            response <- Ok(
                                s"""Multipart Data\nParts:${m.parts.length}
                                     |File contents: ${contents}""".stripMargin)
                        } yield response
                }
            }
    }

}

object TodoRoute {

    object QueryParam {

        object TodoListLimit extends QueryParamDecoderMatcher[Long]("limit")

        object TodoListOffset extends QueryParamDecoderMatcher[Long]("from")

    }

    object Data {

        case class DescriptionOfTodo(value : String)

    }

    case class UserDef(name : String, age : Int)

}