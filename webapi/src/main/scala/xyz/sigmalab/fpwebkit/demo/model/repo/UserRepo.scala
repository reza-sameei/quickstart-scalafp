package xyz.sigmalab.fpwebkit.demo.model.repo

import doobie._
import doobie.implicits._
import cats.effect._
import cats.effect.implicits._
import cats._
import cats.implicits._
import xyz.sigmalab.fpwebkit.demo.model.data

import scala.annotation.switch

class UserRepo extends UserRepo.Base {

    def byId(internal: Long) : ConnectionIO[Option[data.User]] =
        qByInternal(internal).option

    def byPublic(namespace: String, identity: String) : ConnectionIO[Option[data.User]] =
        qByPublic(namespace, identity).option

    def addUser(user: data.NotRegistered) : ConnectionIO[data.User] =
        updateUser(qInsert(user))

    def setDeleted(user: data.User) : ConnectionIO[data.User] =
        updateUser(qSetDeleted(user))
}


object UserRepo {

    trait UserStateMap {

        /*
        The 'final' cause the 'userStateFromInt' compile without warning:
        'could not emit switch for @switch annotated match'
        */

        final val UserStateMap_ACTIVE = 101
        final val UserStateMap_BLOCKED = 201
        final val UserStateMap_DELETED = 301
        final val UserStateMap_NOT_ACTIVATED_YET = 401

        def userStateToInt(st: data.User.State): Int = (st: @switch) match {
            case data.User.State.Active => UserStateMap_ACTIVE
            case data.User.State.Blocked => UserStateMap_BLOCKED
            case data.User.State.Deleted => UserStateMap_DELETED
            case data.User.State.NotActivatedYet => UserStateMap_NOT_ACTIVATED_YET
        }

        def userStateFromInt(st: Int): data.User.State = (st: @switch) match {
            case `UserStateMap_ACTIVE` => data.User.State.Active
            case `UserStateMap_BLOCKED` => data.User.State.Blocked
            case `UserStateMap_DELETED` => data.User.State.Deleted
            case `UserStateMap_NOT_ACTIVATED_YET` => data.User.State.NotActivatedYet
        }
    }

    trait Util { self: UserStateMap =>

        protected type MiddleType = (Long, String, String, Int)

        protected val middleConvertor : Function1[(Long, String, String, Int), data.User] = {
            case (internal: Long, ns: String, id: String, state: Int) =>
                data.User(internal, ns, id, userStateFromInt(state))
        }

        protected val columns = Array("internal_id", "public_namespace", "public_identity", "state")

        protected def queryUser(f: Fragment): Query0[data.User] =
            f.query[MiddleType].map(middleConvertor)

        protected def updateUser(u: doobie.Update0): ConnectionIO[data.User] =
            u.withUniqueGeneratedKeys[MiddleType](columns: _*).map(middleConvertor)

    }

    trait UserQuery { self: Util with UserStateMap =>

        val createTable = Seq(
            sql"DROP TABLE IF EXISTS user_base".update.run,
            sql"""CREATE TABLE user_base (
                   internal_id          serial,
                   public_namespace     varchar,
                   public_identity      varchar,
                   state                int,
                   PRIMARY KEY          (internal_id)
                )""".update.run
            , {
                fr"CREATE INDEX user_base_public_mix ON user_base(public_namespace, public_identity)"
            }.update.run

        )

        protected  val selectAll =
            fr"""SELEcT internal_id, public_namespace, public_identity, state FROM user_base"""

        def qByInternal(internal: Long) : Query0[data.User] =
            queryUser { selectAll ++ fr"WHERE internal_id = $internal" }

        def qByPublic(namespace: String, identity: String) : Query0[data.User] =
            queryUser {
                selectAll ++
                    fr"WHERE public_namespace = $namespace AND public_identity = $identity"
            }

        def qInsert(user: data.NotRegistered) : doobie.Update0 = {
            sql"""INSERT INTO user_base (public_namespace, public_identity, state)
            VALUES (${user.namespace}, ${user.public}, ${userStateToInt(user.state)})"""
        }.update

        def qSetDeleted(user: data.User) : doobie.Update0 = {
            sql"""UPDATE user_base SET state = ${userStateToInt(data.User.State.Deleted)}
            WHERE internal_id = ${user.internal}"""
        }.update
    }

    trait Base extends UserQuery with UserStateMap with Util

}