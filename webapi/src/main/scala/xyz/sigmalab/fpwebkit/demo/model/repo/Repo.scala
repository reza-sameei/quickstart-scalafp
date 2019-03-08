package xyz.sigmalab.fpwebkit.demo.model.repo

import scala.annotation.switch

import doobie._
import doobie.implicits._
import xyz.sigmalab.fpwebkit.demo.model.data


object Repo {

    trait SingleTable {
        def tableName: String
    }

    trait Setup {
        def setup: Seq[ConnectionIO[Int]]
        def cleanup: Seq[ConnectionIO[Int]]
    }

    trait Query {}

}

object Example {

    trait UserRepoSetup extends Repo.Setup with Repo.SingleTable {

        protected val indexNameForPublicMixId = tableName + "_public_mix_id"

        override val setup = Seq(
            {fr"CREATE TABLE" ++ Fragment.const(tableName) ++
                fr"""(
                  internal_id          bigserial NOT NULL,
                  public_namespace     varchar NOT NULL,
                  public_identity      varchar NOT NULL,
                  email                varchar UNIQUE,
                  phone                varchar UNIQUE,
                  state                int NOT NULL,
                  created_at           TIMESTAMP NOT NULL,
                  PRIMARY KEY          (internal_id)
                  )"""}.update.run,
            {fr"CREATE INDEX" ++ Fragment.const(indexNameForPublicMixId) ++
                  fr"""ON user_base(public_namespace, public_identity)"""
            }.update.run
        )

        override val cleanup = Seq(
            {fr"DROP INDEX IF EXISTS"  ++ Fragment.const(indexNameForPublicMixId) }.update.run,
            {fr"DROP TABLE IF EXISTS" ++ Fragment.const(tableName) }.update.run
        )
    }

    object UserRepoUtil {
        /*
        The 'final' cause the 'userStateFromInt' compile without warning:
        'could not emit switch for @switch annotated match'
        */

        final val UserStateMap_ACTIVE = 101
        final val UserStateMap_BLOCKED = 201
        final val UserStateMap_DELETED = 301
        final val UserStateMap_NOT_ACTIVATED_YET = 401
        final val UserStateMap_SUSPENDED = 501

        def userStateToInt(st: data.User.State): Int = (st: @switch) match {
            case data.User.State.Active => UserStateMap_ACTIVE
            case data.User.State.Blocked => UserStateMap_BLOCKED
            case data.User.State.Deleted => UserStateMap_DELETED
            case data.User.State.NotActivatedYet => UserStateMap_NOT_ACTIVATED_YET
            case data.User.State.Suspended => UserStateMap_SUSPENDED
        }

        def userStateFromInt(st: Int): data.User.State = (st: @switch) match {
            case `UserStateMap_ACTIVE` => data.User.State.active
            case `UserStateMap_BLOCKED` => data.User.State.blocked
            case `UserStateMap_DELETED` => data.User.State.deleted
            case `UserStateMap_NOT_ACTIVATED_YET` => data.User.State.notActivatedYet
            case `UserStateMap_SUSPENDED` => data.User.State.suspended
        }

        implicit val $userState$Get: Get[data.User.State] = Get[Int].map(userStateFromInt)
        implicit val $userState$Put: Put[data.User.State] = Put[Int].contramap(userStateToInt)
    }

    trait UserRepoQuery extends Repo.Query with Repo.SingleTable {

        import UserRepoUtil._

        protected val selectAll =
            fr"""SELECT internal_id,
                 public_namespace, public_identity,
                 email, phone,
                 state, created_at
                 FROM""" ++ Fragment.const(tableName)

        def qByInternal(internal: Long) : Query0[data.UserBase] = {
            selectAll ++ fr"WHERE internal_id = $internal"
        }.query[data.UserBase]

        def qByPublic(namespace: String, identity: String) : Query0[data.UserBase] = {
            selectAll ++ fr"WHERE public_namespace = $namespace AND public_identity = $identity"
        }.query[data.UserBase]

        def qInsert(user: data.UserBase) : doobie.Update0 = {
            fr"INSERT INTO" ++ Fragment.const(tableName) ++
            fr"""(
                  public_namespace, public_identity,
                  email, phone,
                  state, created_at
                  ) VALUES (
                  ${user.namespace}, ${user.public},
                  ${user.email}, ${user.phone},
                  ${user.state}, ${user.createdAt}
                  )"""
        }.update

        def qUpdateState(user: data.UserBase) : doobie.Update0 = {
            fr"UPDATE" ++ Fragment.const(tableName) ++
                fr"""
                  SET state = ${user.state}
                  WHERE internal_id = ${user.internal.get}
                """
        }.update

    }

    class UserRepo(override val tableName : String) extends UserRepoQuery

    trait TodoRepoSetup extends Repo.Setup with Repo.SingleTable {

        override val cleanup = Seq(
            {fr"DROP TABLE IF EXISTS " ++ Fragment.const(tableName) }.update.run
        )

        override val setup = Seq(
            {
                fr"CREATE TABLE" ++ Fragment.const(tableName) ++
                    fr"""(
                          internal      bigserial   PRIMARY KEY NOT NULL,
                          desc          varchar (300) NOT NULL,
                          state         tinyint NOT NULL
                          )"""
            }.update.run
        )
    }

    trait TodoRepoQuery extends Repo.Query with Repo.SingleTable {

        protected val selectAll =
            fr"SELECT internal, desc, state FROM " ++ Fragment.const(tableName)

        def qInsert(desc: String, st: )

    }

}
