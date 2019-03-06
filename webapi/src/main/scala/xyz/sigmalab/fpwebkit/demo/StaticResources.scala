package xyz.sigmalab.fpwebkit.demo

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import java.io.File
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

/**
  * @see https://http4s.org/v0.20/static/
  */
trait StaticResources {

    def staticResourcesBlockingEC: ExecutionContext =
        ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

    def basePath : String

    implicit private val cs: ContextShift[IO] = IO.contextShift(staticResourcesBlockingEC)

    def fromPath(file: String, blockingEc: ExecutionContext, request: Request[IO]) =
        StaticFile.fromFile(
            new File(s"${basePath}/to/${file}"),
            staticResourcesBlockingEC,
            Some(request)
        ).getOrElseF(NotFound()) // // In case the file doesn't exist

    def fromJar(file: String, blockingEc: ExecutionContext, request: Request[IO]) =
        StaticFile.fromResource(
            "/" + file, blockingEc, Some(request)
        ).getOrElseF(NotFound()) // // In case the file doesn't exist


    val staticResourcesRoutes = HttpRoutes.of[IO] {
        case request @ GET -> Root / "index.html" =>
            fromPath("index.html", staticResourcesBlockingEC, request)
    }

}
