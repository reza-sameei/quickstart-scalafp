package xyz.sigmalab.fpwebkit.demo

import java.util.concurrent.Executors

import xyz.sigmalab.fpwebkit.util.PgSQL
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.server.Router

import scala.concurrent.ExecutionContext


object Launcher extends IOApp with PgSQL with HelloWorldService with StaticResources {

    override val resourceTx =
        txPoll[IO]("jdbc:postgresql://192.168.1.106:5432/world", "testuser", "testpass", 32)

    // val services = tweetService <+> helloWorldService

    override val basePath = "/var/lib/http-public"

    override val staticResourcesBlockingEC
        = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(100))

    val httpApp = Router(
        "/api" -> helloWorldRoutes,
        // "/api" -> helloWorldRoutes,
        // "/question" -> helloWorldRoutes,
        "/static" -> staticResourcesRoutes
    ).orNotFound


    val serverBuilder =
        BlazeServerBuilder[IO]
            .bindHttp(8080, "localhost")
            .withHttpApp(httpApp)

    val fiber =
        serverBuilder
            .resource.use(_ => IO.never)
            .start.unsafeRunSync()

    override def run(args : List[String]) : IO[ExitCode] =
        fiber.join.map{ _ =>
            staticResourcesBlockingEC.shutdown()
        }.map{_ =>
            ExitCode.Success
        }
}

