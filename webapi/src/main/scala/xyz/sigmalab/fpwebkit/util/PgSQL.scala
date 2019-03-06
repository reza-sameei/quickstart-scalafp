package xyz.sigmalab.fpwebkit.util

import cats.effect.{ContextShift, Effect}
import doobie.{ConnectionIO, FC, Transactor}
import cats.implicits._
import doobie.implicits._

import scala.language.higherKinds

trait PgSQL {

    val DRIVER_NAME = "org.postgresql.Driver"

    def txPoll[F[_] : cats.effect.Effect : cats.effect.ContextShift](
        url : String, username : String, password : String,
        num : Int
    ) : PgSQL.Resource[F] = {
        import doobie.hikari.HikariTransactor
        import doobie.ExecutionContexts
        for {
            ce <- ExecutionContexts.fixedThreadPool[F](32)
            te <- ExecutionContexts.cachedThreadPool[F]
            xa <- HikariTransactor.newHikariTransactor[F](
                DRIVER_NAME,
                url, username, password,
                ce, // await connection here
                te // execute JDBC operations here
            )
        } yield xa
    }

    def txOne[F[_] : Effect : ContextShift](url : String, username : String, pass : String): PgSQL.Tx[F] = {
        Transactor.fromDriverManager[F](DRIVER_NAME, url, username, pass)
    }

    /**
      * create a Transactor to not commit any transaction
      *
      * @param xa
      * @tparam F
      * @return
      */
    def txRoolback[F[_] : cats.effect.Effect](xa : doobie.Transactor[F]): PgSQL.Tx[F] =
        Transactor.after.set(xa, doobie.HC.rollback)

    //
    /**
      * Take a program `p` and return an equivalent one that first commits
      * any ongoing transaction, runs `p` without transaction handling, then
      * starts a new transaction.
      *
      * @see https://tpolecat.github.io/doobie/docs/17-FAQ.html#how-do-i-run-something-outside-of-a-transaction
      */
    def withoutTransaction[A](p : ConnectionIO[A]) : ConnectionIO[A] =
        FC.setAutoCommit(true) *> p <* FC.setAutoCommit(false)


}

object PgSQL extends PgSQL {
    type Resource[F[_]] = cats.effect.Resource[F, Transactor[F]]
    type Tx[F[_]] = Transactor[F]
}
