package xyz.sigmalab.fptemplate.demov1.webapi

import cats.effect._
import doobie.util.log.LogHandler
import org.scalactic.source.Position
import org.scalatest.TestSuite
import xyz.sigmalab.fptemplate.demov1.webapi.TodoRoute.UserDef
// import cats.effect.implicits._
import org.http4s._
import org.http4s.implicits._
import org.scalatest._
import org.http4s.Status
import xyz.sigmalab.fptemplate.demov1.model.repo.TodoRepo
import xyz.sigmalab.fptemplate.demov1.model.serv.TodoService
import xyz.sigmalab.fptemplate.demov1.util.PgSQL

import scala.concurrent.ExecutionContext

trait Http4sSuiteTemplate extends MustMatchers with BeforeAndAfterAll { self : TestSuite =>

    /*
    [error] .../webapi/TodoRouteSuite.scala:19:59:
    This method uses a macro to verify that a String literal is a valid URI.
    Use Uri.fromString if you have a dynamic String that you want to parse as a Uri.
    [error]     def get(uri: String) = Request[IO](Method.GET, Uri.uri(uri))
    [error]                                                           ^
    */
    // def get(uri: String) = Request[IO](Method.GET, Uri.uri(uri))
    def get(uri: String) = Request[IO](Method.GET, Uri.fromString(uri).right.get)

    implicit class OpsOfResponseIO(val resp: Response[IO]) {
        def mustMatchStatus(expt: Status)(implicit pos: Position): Assertion = assert(resp.status == expt, resp)
        def bodyString = {
            val bytes = resp.body.compile.toVector.unsafeRunSync.toArray
            new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
        }
    }
}

class TodoRouteSuite extends FlatSpec with Http4sSuiteTemplate {

    implicit val cs = IO.contextShift(ExecutionContext.global)

    def transactor : doobie.Transactor[IO] =
        doobie.Transactor.fromDriverManager[IO](
            "org.postgresql.Driver",
            "jdbc:postgresql://192.168.1.106:5432/world",
            "testuser", "testpass"
        )

    val todoRoute = new TodoRoute {

        override def todoService : TodoService =
            new TodoService(new TodoRepo("todo_repo_table", LogHandler.nop))

        override def resourceTx : Resource[IO, doobie.Transactor[IO]] =
            new PgSQL {}.txPoll(
            "jdbc:postgresql://192.168.1.106:5432/world",
            "testuser", "testpass", 1
            )
    }

    it must "return not found" in {
        /*val getHW = Request[IO](Method.GET, Uri.uri("/hello/world"))
        val resp = todoRoute.todoRoute.orNotFound(getHW).unsafeRunSync*/
        val resp = todoRoute.todoRoute.orNotFound(get("/hello/world")).unsafeRunSync
        info(s"Body: '${resp.bodyString}'")
        resp mustMatchStatus Status.NotFound
    }

    it must "return a json" in {
        import io.circe._
        import org.http4s.circe._
        val req = Request[IO](Method.GET, Uri.uri("/debug/return_json"))
        val resp = todoRoute.todoRoute.orNotFound(req).unsafeRunSync
        info(resp.toString)
        val body = resp.as[Json].unsafeRunSync
        info(body.toString)
    }

    it must "return a json & conver to case-class" in {
        import io.circe._
        import io.circe.generic.auto._
        import org.http4s.circe._
        // implicit val a = Decoder[TodoRoute.UserDef]
        implicit val x = jsonOf[IO, TodoRoute.UserDef]
        val req = Request[IO](Method.GET, Uri.uri("/debug/return_json_v2"))
        val resp = todoRoute.todoRoute.orNotFound(req).flatMap(_.as[TodoRoute.UserDef]).unsafeRunSync
        info(resp.toString)
    }

    it must "return body" in {
        import io.circe.literal._
        import org.http4s.circe._

        // import org.http4s.circe.CirceEntityEncoder
        // val x = CirceEntityEncoder.circeEntityEncoder[IO, UserDef]
        // import io.circe.generic.auto._
        // implicit val x = jsonEncoderOf[IO, UserDef]

        val postJSON = Request[IO](
            Method.POST,
            Uri.uri("/debug/show_body")
        // ).withBodyStream(json"""{"name": "reza", "age": 30}""")
        // ).withBody(json"""{"name": "reza", "age": 30}""")
        ).withEntity(json"""{"name": "reza", "age": 30}""")

        val resp = todoRoute.todoRoute.orNotFound(postJSON).unsafeRunSync

        info(resp.toString)
        info(resp.bodyString)

    }

    it must "send data as json" in {
        import io.circe.literal._
        import org.http4s.circe._
        import io.circe._, io.circe.syntax._
        import io.circe.generic.auto._

        // import org.http4s.circe.CirceEntityEncoder
        // val x = CirceEntityEncoder.circeEntityEncoder[IO, UserDef]
        // import io.circe.generic.auto._
        // implicit val x = jsonEncoderOf[IO, UserDef]

        val postJSON = Request[IO](
            Method.POST,
            Uri.uri("/debug/ops")
        ).withEntity(UserDef("Reza", 30).asJson)

        val resp = todoRoute.todoRoute.orNotFound(postJSON).unsafeRunSync

        info(resp.toString)
        info(resp.bodyString)
        resp mustMatchStatus Status.Ok

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
