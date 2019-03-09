package xyz.sigmalab.fptemplate.demov1.webapi

import cats.effect._
// import cats.effect.implicits._
import org.http4s._
import org.http4s.implicits._
import org.scalatest._
import xyz.sigmalab.fptemplate.demov1.model.repo.TodoRepo
import xyz.sigmalab.fptemplate.demov1.model.serv.TodoService
import xyz.sigmalab.fptemplate.demov1.util.PgSQL

import scala.concurrent.ExecutionContext

class TodoRouteSuite extends FlatSpec with MustMatchers with BeforeAndAfterAll {

    implicit val cs = IO.contextShift(ExecutionContext.global)

    def transactor : doobie.Transactor[IO] =
        doobie.Transactor.fromDriverManager[IO](
            "org.postgresql.Driver",
            "jdbc:postgresql://192.168.1.106:5432/world",
            "testuser", "testpass"
        )

    val todoRoute = new TodoRoute {

        override def todoService : TodoService =
            new TodoService(new TodoRepo("todo_repo_table"))

        override def resourceTx : Resource[IO, doobie.Transactor[IO]] =
            new PgSQL {}.txPoll(
            "jdbc:postgresql://192.168.1.106:5432/world",
            "testuser", "testpass", 1
            )
    }

    it must "?" in {
        val getHW = Request[IO](Method.GET, Uri.uri("/hello/world"))
        val resp = todoRoute.todoRoute.orNotFound(getHW).unsafeRunSync
        resp.status mustEqual org.http4s.Status.NotFound
        // resp.status mustEqual org.http4s.Status.Ok
    }



    /*def check[A](actual:        IO[Response[IO]],
        expectedStatus: Status,
        expectedBody:   Option[A])(
        implicit ev: EntityDecoder[IO, A]
    ): Boolean =  {
        val actualResp         = actual.unsafeRunSync
        val statusCheck        = actualResp.status == expectedStatus
        val bodyCheck          = expectedBody.fold[Boolean](
            actualResp.body.compile.toVector.unsafeRunSync.isEmpty)( // Verify Response's body is empty.
            expected => actualResp.as[A].unsafeRunSync == expected
        )
        statusCheck && bodyCheck
    }
    */

}
