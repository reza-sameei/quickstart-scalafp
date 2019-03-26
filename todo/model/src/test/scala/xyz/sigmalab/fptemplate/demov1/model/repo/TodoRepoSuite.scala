package xyz.sigmalab.fptemplate.demov1.model.repo

import doobie.util.log.LogHandler

import scala.concurrent.ExecutionContext
// import cats._
import cats.implicits._
import cats.effect._
// import cats.effect.implicits._
import org.scalatest._
// import doobie._
import doobie.implicits._
import doobie.scalatest._
import xyz.sigmalab.fptemplate.demov1.model.data.{TodoItem, TodoState}

class TodoRepoSuite extends FlatSpec with MustMatchers with IOChecker with BeforeAndAfterAll {

    implicit val cs = IO.contextShift(ExecutionContext.global)

    override def transactor : doobie.Transactor[IO] =
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
        override def tableName : String = "todo_test"
    }

    override def beforeAll() : Unit = {
        val list = baserep.cleanup ++ baserep.setup
        val all = list.tail.foldLeft(list.head) { (s,i) => s *> i }
        transactor.trans.apply(all).unsafeRunSync
    }

    override def afterAll(): Unit = {
        return;
        val list = baserep.cleanup
        val all = list.tail.foldLeft(list.head) { (s,i) => s *> i }
        transactor.trans.apply(all).unsafeRunSync
    }


    val trivial = sql"""select 42, 'foo'::varchar""".query[(Int, String)]

    "triavial" must "trivial" in { check(trivial) }

    /* "todorepo" must "setup" in {
        val list = repo.cleanup ++ repo.setup
        val all = list.tail.foldLeft(list.head) { (s,i) => s *> i }
        transactor.trans.apply(all).unsafeRunSync
    } */

    val item = TodoItem(orgId = 1, description = "Write Todo with DoobieHttp4", TodoState.inList, id = None)

    "todorepo" must "check insert" in { check(baserep.qInsert(item)) }

    "todorepo" must "check update" in { check(baserep.qUpdate(id = 1, item)) }

    "todorepo" must "check list" in { check(baserep.qList(orgId = 1, Range.Long(0, 2, 1))) }

    "todorepo" must "add & query" in {

        val repo = new TodoRepo(baserep.tableName, LogHandler.jdkLogHandler)

        val prog = for {
            a <- repo.add(org = 1, desc = "Write Todo Example")
            b <- repo.add(org = 1, desc = "Write Tests")
            c <- repo.list(
                org = 1,
                range = Range.Long(start = 0, end = 2, step = 1) /* eclusive range */
            )
        } yield (a,b,c)

        val result @ (a,b, c) = prog.transact(transactor).unsafeRunSync()
        info(result.toString)
        c.size == 2
        a :: b :: Nil mustEqual c.toList
    }
}

