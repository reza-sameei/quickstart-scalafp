package xyz.sigmalab.fptemplate.demov1.model.repo

import doobie.ConnectionIO

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
