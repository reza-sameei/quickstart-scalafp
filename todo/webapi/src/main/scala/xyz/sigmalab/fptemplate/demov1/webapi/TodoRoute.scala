package xyz.sigmalab.fptemplate.demov1.webapi

import cats.effect._
import org.http4s._
// import io.circe._, io.circe.literal._, io.circe.parser._
import io.circe.generic.auto._, io.circe.syntax._
// import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.dsl.io._


import xyz.sigmalab.fptemplate.demov1.model.serv.TodoService

trait TodoRoute {

    // def todoTx: Resource[IO, doobie.Transactor[IO]]

    def todoService: TodoService

    def resourceTx: Resource[IO, doobie.Transactor[IO]]

    object QueryParam{
        object TodoListLimit extends QueryParamDecoderMatcher[Long]("limit")
        object TodoListOffset extends QueryParamDecoderMatcher[Long]("from")
    }

    def todoRoute = HttpRoutes.of[IO] {

        case GET -> Root / "todo" / LongVar(org)
            :? QueryParam.TodoListLimit(limit)
            +& QueryParam.TodoListOffset(offset) =>

            val a = resourceTx.use { implicit tx => for {
                list <- todoService.list(org, Range.Long(offset, offset + limit, 1))
            } yield list.asJson }
            val b = Ok(a)

            /*
            val c = resourceTx.use { implicit tx => for {
                list <- todoService.list(org, Range.Long(offset, offset + limit, 1))
                result <- Ok(list.asJson)
            } yield result }
            */

            b
    }

}
