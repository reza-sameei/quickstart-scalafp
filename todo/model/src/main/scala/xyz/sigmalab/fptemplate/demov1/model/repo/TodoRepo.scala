package xyz.sigmalab.fptemplate.demov1.model.repo

import doobie._
import doobie.implicits._
import xyz.sigmalab.fptemplate.demov1.model.data

import scala.collection.immutable.NumericRange

class TodoRepo(
    override val tableName : String,
    override implicit val logHandler: LogHandler
) extends TodoRepo.TodoRepoQuery {

    import TodoRepo.TodoRepoUtil._

    def add(org: Long, desc: String) : ConnectionIO[data.TodoItem] =
        qInsert(data.TodoItem(org, desc, data.TodoState.inList, None))
            .withUniqueGeneratedKeys[data.TodoItem](columns: _*)


    def list(org: Long, range: NumericRange[Long]) : ConnectionIO[Seq[data.TodoItem]] =
        qList(org, range).to[Seq]

}

object TodoRepo {

    object TodoRepoUtil {

        final val TodoStateMap_IN_LIST : Short = 10
        final val TodoStateMap_DONE : Short  = 20

        def todoStateToInt(st: data.TodoState) : Short = st match {
            case data.TodoState.InList => TodoStateMap_IN_LIST
            case data.TodoState.Done => TodoStateMap_DONE
        }

        def todoStateFromInt(st: Short) : data.TodoState = (st : @scala.annotation.switch) match {
            case `TodoStateMap_IN_LIST` => data.TodoState.inList
            case `TodoStateMap_DONE` => data.TodoState.done
        }

        implicit val $todoState$Get : Get[data.TodoState] = Get[Short].map(todoStateFromInt)
        implicit val $todoState$Put : Put[data.TodoState] = Put[Short].contramap(todoStateToInt)

    }

    trait TodoRepoSetup extends Repo.Setup with Repo.SingleTable {

        override val cleanup = Seq(
            { fr"DROP TABLE IF EXISTS" ++ Fragment.const(tableName) }.update.run
        )

        override val setup = Seq(
            { fr"CREATE TABLE" ++ Fragment.const(tableName) ++
                fr"""(
                      id            bigserial PRIMARY KEY NOT NULL,
                      org_id        bigint NOT NULL,
                      description   varchar (200) NOT NULL,
                      state         smallint NOT NULL
                      )"""
            }.update.run
        )

    }

    trait TodoRepoQuery extends Repo.Query with Repo.SingleTable {

        import TodoRepoUtil._

        protected val selectAll =
            fr"SELECT org_id, description, state, id FROM" ++ Fragment.const(tableName)

        protected val columns = Array("org_id", "description", "state", "id")

        def qInsert(item: data.TodoItem): Update0 = {
            fr"INSERT INTO" ++ Fragment.const(tableName) ++
                fr"""(org_id, description, state) VALUES (${item.orgId}, ${item.description}, ${item.state})"""
        }.update

        def qUpdate(id: Long, item: data.TodoItem) : Update0 = {
            fr"UPDATE" ++ Fragment.const(tableName) ++
                fr"""SET org_id = ${item.orgId},
                     description = ${item.description},
                     state = ${item.state}
                     WHERE id = ${id}
                  """
        }.update

        def qList(orgId: Long, range: NumericRange[Long]) : Query0[data.TodoItem] = {
            selectAll ++ fr"WHERE org_id = $orgId LIMIT ${range.size.toLong} OFFSET ${range.min}"
        }.query[data.TodoItem]
    }

}
