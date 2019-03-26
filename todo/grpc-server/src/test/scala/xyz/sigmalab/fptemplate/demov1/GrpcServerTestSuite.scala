package xyz.sigmalab.fptemplate.demov1

import org.scalatest._
import cats.implicits._
import cats.effect._
import doobie.implicits._
import doobie.util.log.LogHandler
import io.grpc.protobuf.services.ProtoReflectionService
import xyz.sigmalab.fptemplate.demov1.model.repo.TodoRepo
import xyz.sigmalab.fptemplate.demov1.model.serv.TodoService
import xyz.sigmalab.fptemplate.demov1.proto.todo.{TodoFs2Grpc, TodoNewItem}
import xyz.sigmalab.temp.TodoGrpcService
// import org.lyranthe.fs2_grpc.java_runtime.implicits._

import scala.concurrent.ExecutionContext

class GrpcServerTestSuite extends FlatSpec with MustMatchers with BeforeAndAfterAll {

  import ExecutionContext.Implicits.global
  implicit val cs = IO.contextShift(ExecutionContext.global)

  def transactor : doobie.Transactor[IO] =
    doobie.Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://192.168.1.107:5432/world",
      "testuser", "testpass"
    )

  /*
  val y = transactor.yolo
  import y._
  */

  val baserep = new TodoRepo.TodoRepoSetup with TodoRepo.TodoRepoQuery {
    implicit override def logHandler = LogHandler.jdkLogHandler
    override def tableName : String = "todo_test_v2"
  }

  override def beforeAll() : Unit = {
    val list = baserep.cleanup ++ baserep.setup
    val all = list.tail.foldLeft(list.head) { (s,i) => s *> i }
    transactor.trans.apply(all).unsafeRunSync
    ()
  }

  override def afterAll(): Unit = {
    return;
    val list = baserep.cleanup
    val all = list.tail.foldLeft(list.head) { (s,i) => s *> i }
    transactor.trans.apply(all).unsafeRunSync
    ()
  }

  it must "?" in {

    val repo = new TodoRepo(baserep.tableName, LogHandler.jdkLogHandler)
    val service = new TodoService(repo)
    val todoServiceDef = TodoFs2Grpc.bindService(new TodoGrpcService(service, transactor))

    val server: IO[io.grpc.Server] = IO{
      io.grpc.ServerBuilder
        .forPort(10021)
        .addService(todoServiceDef)
        .addService(ProtoReflectionService.newInstance)
        .build().start()
    }


    val client: IO[TodoFs2Grpc[IO, io.grpc.Metadata]] = IO {
      io.grpc.ManagedChannelBuilder
        .forAddress("127.0.0.1", 10021)
        .usePlaintext()
        .build
    } map { channel =>
      TodoFs2Grpc.stub(channel)
    }

    val rsl = for {
      s <- server
      c <- client
      x <- c.addAll(
        fs2.Stream(TodoNewItem(1, "Hello"), TodoNewItem(1, "Hello-2")),
        new io.grpc.Metadata()
      )
    } yield {
      info(x.toString)
    }


    rsl.unsafeRunSync()

  }

}
