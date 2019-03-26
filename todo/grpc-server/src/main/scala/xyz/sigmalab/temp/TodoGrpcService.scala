package xyz.sigmalab.temp

import xyz.sigmalab.fptemplate.demov1.proto.todo
import xyz.sigmalab.fptemplate.demov1.proto.todo.{AddOneResponse, AddlAllResponse, TodoFs2Grpc, TodoNewItem}
import cats.effect._
import xyz.sigmalab.fptemplate.demov1.model.serv.TodoService
import xyz.sigmalab.fptemplate.demov1.model.data.{TodoItem, TodoState}

class TodoGrpcService[CTX](
  service: TodoService,
  implicit private val transactor: doobie.Transactor[IO],
) extends TodoFs2Grpc[IO, CTX] {

  def convert(i: TodoItem) = {
    todo.TodoItem(
      i.orgId, i.id.get, i.description,
      i.state match {
        case TodoState.InList => todo.TodoItemState.TodoItemState_InList
        case TodoState.Done => todo.TodoItemState.TodoItemState_Done
      }
    )
  }

  override def addAll(
    request : fs2.Stream[IO, TodoNewItem],
    context: CTX
  ) : IO[AddlAllResponse] = {
    
    request.fold(List.empty[TodoNewItem]) { (buf, i) => i :: buf }.flatMap { all =>
      fs2.Stream.eval[IO, Seq[TodoItem]]{ service.addAll(all.map{ i => i.org -> i.description }) }
    }.map {
      ls => AddlAllResponse(ls.map(convert _))
    }.compile.last.map{_.get}


  }

  override def addOne(
    request : TodoNewItem,
    context: CTX
  ) : IO[AddOneResponse] = {
    service.add(request.org, request.description).map(convert).map { i => AddOneResponse(Some(i)) }
  }
}
