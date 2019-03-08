package xyz.sigmalab.fpwebkit.demo.model.data

case class TodoItem(orgId: Long, description: String, state: TodoState, id: Option[Long])

sealed trait TodoState

object TodoState {

    final case object InList extends TodoState
    def inList : TodoState = InList

    final case object Done extends TodoState
    def done : TodoState = Done
}