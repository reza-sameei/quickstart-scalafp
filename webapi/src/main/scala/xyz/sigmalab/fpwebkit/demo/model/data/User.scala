package xyz.sigmalab.fpwebkit.demo.model.data

case class User(internal: Long, namespace: String, public: String, state: User.State)

object User {

    sealed trait State
    object State {

        final case object NotActivatedYet extends State
        def notActivatedYet : State = NotActivatedYet

        final case object Active extends State
        def active : State = Active

        final case object Blocked extends State
        def blocked : State = Blocked

        final case object Deleted extends State
        def deleted : State = Deleted

        final case object Suspended extends State
        def suspended : State = Suspended
    }

}

case class NotRegistered(
    namespace: String, public: String,
    state: User.State = User.State.NotActivatedYet
)


case class UserBase private (
    internal: Option[Long],
    namespace: String, public: String,
    email: Option[String], phone: Option[String],
    state: User.State,
    // createdAt: java.time.LocalDateTime
    createdAt: java.sql.Timestamp
)