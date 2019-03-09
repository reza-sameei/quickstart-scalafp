package xyz.sigmalab.fptemplate.demov1.model.serv

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import xyz.sigmalab.fptemplate.demov1.model.repo.TodoRepo
import xyz.sigmalab.fptemplate.demov1.model.data.TodoItem

import scala.collection.immutable.NumericRange

class TodoService(todoRepo: TodoRepo) {

    def add(orgId: Long, desc: String)(implicit tx: doobie.Transactor[IO]) : IO[TodoItem] =
        todoRepo.add(orgId, desc).transact(tx)

    def addAll(todos: Seq[(Long, String)])(implicit tx: doobie.Transactor[IO]): IO[List[TodoItem]] = {
        val ios = todos.map{
            case (org, desc) => todoRepo.add(org, desc)
        }.toList
        val io = cats.Traverse[List].sequence(ios)
        io.transact(tx)
    }

    def list(orgId: Long, range: NumericRange[Long])(implicit tx: doobie.Transactor[IO]) : IO[List[TodoItem]] =
        todoRepo.list(orgId, range).transact(tx).map{_.toList}

}
