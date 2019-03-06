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
        val list = userRepo.cleanUp ++ userRepo.setUp
        val fl = list.tail.foldLeft(list.head){ (s,i) => s *> i }
        transactor.trans.apply(fl).unsafeRunSync()
    }

    check(userRepo.qByInternal(1))
    check(userRepo.qByPublic("bisphone.com", "reza.sameei"))
    check(userRepo.qInsert(data.NotRegistered("bisphone.com", "reza.sameei")))
    check(userRepo.qSetDeleted(data.User(1, "bisphone.com", "reza.sameei", data.User.State.NotActivatedYet)))



    /*{
        val list = userRepo.cleanUp
        val fl = list.tail.foldLeft(list.head){ (s,i) => s *> i }
        transactor.trans.apply(fl).unsafeRunSync()
    }*/
}