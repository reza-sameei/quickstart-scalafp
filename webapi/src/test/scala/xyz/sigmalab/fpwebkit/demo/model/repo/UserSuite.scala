package xyz.sigmalab.fpwebkit.demo.model.repo

import cats._
import cats.implicits._

import cats.effect._
import cats.effect.implicits._

import doobie._
import doobie.implicits._

import doobie.specs2._
import org.specs2._

import xyz.sigmalab.fpwebkit.demo.model.data

import scala.concurrent.ExecutionContext

class UserSuite extends org.specs2.mutable.Specification with IOChecker {

    implicit val cs = IO.contextShift(ExecutionContext.global)

    override def transactor : doobie.Transactor[IO] =
        doobie.Transactor.fromDriverManager[IO](
            "org.postgresql.Driver",
            "jdbc:postgresql://192.168.1.106:5432/world",
            "testuser", "testpass"
        )

    val y = transactor.yolo
    import y._

    val trivial =  sql""" select 42, 'foo'::varchar """.query[(Int, String)]

    check(trivial)

    val userRepo = new UserRepo.Base {}


    {
        val list = userRepo.createTable
        val fl = list.tail.foldLeft(list.head){ (s,i) => s *> i }
        transactor.trans.apply(fl).unsafeRunSync()
    }

    check(userRepo.qByInternal(1))

    // check(userRepo.addUser(data.NotRegistered("bisphone.com", "reza.sameei")))

    // check(userRepo.addUser(data.NotRegistered("sigmalab.xyz", "reza.sameei")))

    /*

    [error] /mnt/mine/work/trialblaze/quickstart/webapi/src/test/scala/xyz/sigmalab/fpwebkit/demo/model/repo/UserSuite.scala:41:10:
    could not find implicit value for evidence parameter of type
    doobie.util.testing.Analyzable[doobie.ConnectionIO[xyz.sigmalab.fpwebkit.demo.model.data.User]]

    [error] /mnt/mine/work/trialblaze/quickstart/webapi/src/test/scala/xyz/sigmalab/fpwebkit/demo/model/repo/UserSuite.scala:44:10:
    could not find implicit value for evidence parameter of type
    doobie.util.testing.Analyzable[doobie.ConnectionIO[xyz.sigmalab.fpwebkit.demo.model.data.User]]



     */

}


case class Country(code: Int, name: String, pop: Int, gnp: Double)

class CountryRepo {

    val create = sql"""
                        CREATE TABLE country (
                          code        character(3)  NOT NULL,
                          name        text          NOT NULL,
                          population  integer       NOT NULL,
                          gnp         numeric(10,2),
                          indepyear   smallint
                          -- more columns, but we won't use them here
                        )
                       """.update.run

    val trivial = sql"""
  select 42, 'foo'::varchar
""".query[(Int, String)]

    def biggerThan(minPop: Short) = sql"""
  select code, name, population, gnp, indepyear
  from country
  where population > $minPop
""".query[Country]

    def update(oldName: String, newName: String) = sql"""
  update country set name = $newName where name = $oldName
""".update

}