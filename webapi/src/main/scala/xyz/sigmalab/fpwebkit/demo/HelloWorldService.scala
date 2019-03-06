package xyz.sigmalab.fpwebkit.demo


import cats.effect._, org.http4s._, org.http4s.dsl.io._

/*import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.server.Router*/

trait HelloWorldService {

    // https://github.com/IronCoreLabs/http4s-demo

    // Metrics
    // https://github.com/http4s/http4s/blob/master/examples/blaze/src/main/scala/com/example/http4s/blaze/BlazeMetricsExample.scala
    // https://github.com/http4s/http4s/blob/master/server/src/main/scala/org/http4s/server/middleware/Metrics.scala
    // https://index.scala-lang.org/kamon-io/kamon-http4s/kamon-http4s/1.0.11?target=_2.12

    // WebSocket
    // https://github.com/http4s/http4s/blob/master/examples/blaze/src/main/scala/com/example/http4s/blaze/BlazeWebSocketExample.scala

    def resourceTx: Resource[IO, doobie.Transactor[IO]]

    val helloWorldRoutes = HttpRoutes.of[IO] {

        case GET -> Root / "hello" / name =>
            resourceTx.use { tx =>
                //Ok(s"{\"message\":\"Hello, ${name}\"}")
                // Ok(s"Hello, $name.")
                Ok(s"""{"message":"Hello, ${name}"}""")
            }

        case GET -> Root / "bye" / name =>
            resourceTx.use { tx => BadRequest(":X") }

    }

}
