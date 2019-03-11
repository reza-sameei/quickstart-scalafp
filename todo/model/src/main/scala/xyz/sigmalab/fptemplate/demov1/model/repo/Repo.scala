package xyz.sigmalab.fptemplate.demov1.model.repo

import doobie.{ConnectionIO, LogHandler}

object Repo {

    trait SingleTable {
        def tableName: String
    }

    trait Setup {
        implicit def logHandler: LogHandler
        def setup: Seq[ConnectionIO[Int]]
        def cleanup: Seq[ConnectionIO[Int]]
    }

    trait Query {
        implicit def logHandler: LogHandler
    }

}
