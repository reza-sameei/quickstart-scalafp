package xyz.sigmalab.fpwebkit.demo.model.data

case class User(internal: Long, namespace: String, public: String, state: User.State)

object User {

    sealed trait State
    object State {
        final case object NotActivatedYet extends State
        final case object Active extends State
        final case object Blocked extends State
        final case object Deleted extends State
    }

}

case class NotRegistered(
    namespace: String, public: String,
    state: User.State = User.State.NotActivatedYet
)
