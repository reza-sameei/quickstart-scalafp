package com.example.quickstart

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import doobie._
import doobie.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import shapeless._

case class Person2(id: Long, name: String, pets: List[String])

object LearnDoobie extends IOApp {

    import scala.concurrent.ExecutionContext
    val cs = IO.contextShift(ExecutionContext.global)

    val xa = Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql://192.168.1.106:5432/world",
        "testuser",
        "testpass"
    )

    def initPoll(list: List[String]) = {
        import doobie.hikari.HikariTransactor
        for {
            ce <- ExecutionContexts.fixedThreadPool[IO](32)
            te <- ExecutionContexts.cachedThreadPool[IO]
            xa <- HikariTransactor.newHikariTransactor[IO](
                "org.postgresql.Driver",
                "jdbc:postgresql://192.168.1.106:5432/world",
                "testuser",
                "testpass",
                ce,                                     // await connection here
                te                                      // execute JDBC operations here
            )
        } yield xa
    }

    {
        val transactor = initPoll(Nil)

        val a = for {
            xa <- transactor
            xb <- transactor
        } yield ("Hello", xa, xb)


        // a.use { case("Hello", xa, xb) => ??? }
        // match may not be exhaustive.
        a.use { i => (i: @unchecked) match { case("Hello", xa, xb) => ??? }}

        val x = transactor.use { xa => for {
            _ <- xa.trans.apply(???)
            _ <- IO(println("OPS"))
        } yield ??? }

    }


    // rollback
    // https://github.com/tpolecat/doobie/issues/535
    val xb = Transactor.after.set(xa, HC.rollback) // Rollback after every run!

    /*val xb = Transactor.fromDriverManager[Effect](
        "org.postgresql.Driver",
        "jdbc:postgresql://192.168.1.106:5432/world",
        "testuser",
        "testpass"
    )*/

    val program1 = 42.pure[ConnectionIO]

    val program2 = sql"select 42".query[Int].unique

    val program3 = for {
        a <- sql"select 42".query[Int].unique
        b <- sql"select random()".query[Double].unique
    } yield (a,b)

    val program4 =
        sql"select name from country limit 5"
            .query[String]    // Query0[String]
            .to[List]         // ConnectionIO[List[String]]
            .transact(xa)     // IO[List[String]]

    val program5 =
        sql"SELECT name FROM country".query[String].stream  // Stream[ConnectionIO, String]
        .take(5).compile.toList                             // ConnectionIO[List[String]]
        .transact(xa)

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    def program6(xa: Transactor.Aux[IO, Unit]) = {

        val y = xa.yolo
        import y._

        sql"SELECT code, name, population, gnp FROM country"
            .query[Country]
            .stream
            .take(5)
            .quick
    }

    def query7(minPop: Int) =
        sql""" SELECT code, name, population, gnp
               FROM country
               WHERE population > $minPop
           """.query[Country]

    def program7(xa: Transactor.Aux[IO, Unit], minPop: Int) = {
        val y = xa.yolo
        import y._
        query7(minPop).quick
    }

    def query8(range: Range) =
        sql"""SELEcT code, name, population, gnp
              FROM country
              WHERE population > ${range.min}
              AND population < ${range.max}
           """.query[Country]

    def query9(range: Range, codes: NonEmptyList[String]) = {
        fr"""SELECT code, name, population, gnp
             FROM country
             WHERE """ ++ Fragments.in(fr"code", codes) // code IN (....)
    }.query[Country]

    def query10(codes: NonEmptyList[String]) = {
        fr"""SELECT code, name, population, gnp
             FROM country
             WHERE """ ++ Fragments.in(fr"code", codes) // code IN (....)
    }.query[Country]

    def program11(xa: Transactor.Aux[IO, Unit]) = {

        import shapeless.record.Record
        type Rec = Record.`'code -> String, 'name -> String, 'gnp -> Option[Double], 'pop -> Int`.T

        val y = xa.yolo
        import y._


        // You can also nest case classes, HLists, shapeless records, and/or tuples
        // arbitrarily as long as the eventual members are of supported columns types

        sql"SELECT code, name, population, gnp FROM country"
            .query[Rec] // Record, HList, Case class,
            .stream
            .take(5)
            .quick      // quick: sink to stdout + ANSI coloring, return IO[Unit]
                        // check: returns IO[Unit] to perform a metadata analysis
    }

    def undercover: fs2.Stream[IO, Unit] = {
        HC.stream(
            "select code, name, population, gnp from country",
            ().pure[PreparedStatementIO],
            512
        ).transact(xa)
    }

    def undercover2: fs2.Stream[IO, Country] = {

        val a = doobie.Write[(String, Boolean)]
        val b = HPS.set(("nothing", true))
        val c = HPS.set(("nothing", true)) *> HPS.set(("nothing", true))
        val d = FPS.setString(1, "foo") *> FPS.setBoolean(2, true)

        HC.stream[Country](
            "select code, name, population, gnp, from country where population > ? and population < ?",
            HPS.set(1, 10), // Needs a Write[A]
            512
        ).transact(xa)

    }

    class CountryT(tableName: String) {
        val selectAll = fr"SELECT code, name, population, gnp, indepyear FROM ${tableName}"
        def top(n: Int) = { selectAll ++ fr"LIMIT $n" }.query[Country]
    }
    {
        val y = xa.yolo
        import y._
        new CountryT("country").top(12).check
    }


    def biggerThan(minPop: Short) =
        sql"""SELECT code, name, population, gnp, indepyear
              FROM country
              WHERE population > $minPop
           """.query[Country]

    def dataDef(xa: Transactor.Aux[IO, Unit]): IO[(Int, Int)] = {

        val drop = sql"DROP TABLE IF EXISTS person".update.run

        val create = sql"CREATE TABLE person (id SERIAL, name VARCHAR NOT NULL UNIQUE, age SMALLINT)".update.run

        val a = (drop, create).tupled
        val b = a.transact(xa)

        val c = xa.trans.apply(a)

        b
    }

    def insert(name: String, age: Option[Short]): Update0 =
        sql"INSERT INTO person (name, age) VALUES ($name, $age)".update


    def update(name: String, age: Short) =
        sql"UPDATE person SET age = $age WHERE name = $name".update

    case class Person(id: Long, name: String, age: Option[Short])

    def insert2(name: String, age: Option[Short]): ConnectionIO[Person] =
        for {
            _  <- sql"insert into person (name, age) values ($name, $age)".update.run
            id <- sql"select lastval()".query[Long].unique
            p  <- sql"select id, name, age from person where id = $id".query[Person].unique
        } yield p

    def insert2_H2(name: String, age: Option[Short]): ConnectionIO[Person] =
        for {
            id <- sql"insert into person (name, age) values ($name, $age)"
                .update
                .withUniqueGeneratedKeys[Int]("id")
            p  <- sql"select id, name, age from person where id = $id"
                .query[Person]
                .unique
        } yield p

    // Other databases (including PostgreSQL) provide a way to do this in
    // one shot by returning multiple specified columns from the inserted row.
    def insert3(name: String, age: Option[Short]): ConnectionIO[Person] = {
        sql"insert into person (name, age) values ($name, $age)"
            .update
            .withUniqueGeneratedKeys("id", "name", "age")
    }

    // This mechanism also works for updates, for databases that support it. In the case of multiple row updates
    // we omit unique and get a Stream[ConnectionIO, Person] back.
    val up = {
        sql"update person set age = age + 1 where age is not null"
            .update
            .withGeneratedKeys[Person]("id", "name", "age")
    }


    val selectCountry = fr"SELECT code, name, population, gnp FROM country"

    val n = "H"
    val q1 = selectCountry ++ fr"WHERE code = $n"
    val q2 = selectCountry ++ fr"WHERE code = 'USA'"
    val q3 = { fr"SELECT count(*) FROM" ++ Fragment.const("table_name") /* no scaping */ }.query[Int]


    def byName(pat: String) = {
        sql"SELECT name, code FROM country WHERE name like $pat"
            .queryWithLogHandler[String :: String :: HNil](LogHandler.jdkLogHandler)
            .to[List]
            .transact(xa)
    }

    def byX(code: String) = {
        implicit val logH = LogHandler.jdkLogHandler
        sql"SELEcT name, code FROM country WHERE code = $code"
            .query[String :: String :: HNil]
            .to[List]
    }



    def arrayTest(xa: Transactor.Aux[IO, Unit]) = {

        val y = xa.yolo
        import y._
        import shapeless._
        import doobie.implicits._
        import doobie.postgres._
        import doobie.postgres.implicits._

        implicit val logH = LogHandler.jdkLogHandler

        val drop = sql"DROP TABLE IF EXISTS person".update.run
        val create =
            sql"""CREATE TABLE person (
                id SERIAL,
                name VARCHAR NOT NULL UNIQUE,
                pets VARCHAR[] NOT NULL
            )""".update.run

        def insert(name: String, pets: List[String]): ConnectionIO[Person2] = {
            sql"INSERT INTO person (name, pets) VALUES ($name, $pets)"
                .update
                .withUniqueGeneratedKeys("id", "name", "pets")
        }

        // (drop, create).mapN()
        // (drop, create).tupled
        val all = for {
            _ <- drop
            _ <- create
            a <- insert("Bob", "Nixon" :: "Slappy" :: Nil)
            b <- insert("Alice", Nil)
        } yield (a,b)

        all.transact(xa)

        // **** https://tpolecat.github.io/doobie/docs/11-Arrays.html#lamentations-of-null
    }





    override def run(args : List[String]) : IO[ExitCode] = {
        // program1.transact(xa).map{_ => ExitCode.Success}
        /*program3.replicateA(5).transact(xa).map{i =>
            println(s"HERE: ${i}")
            ExitCode.Success
        }*/
        // program4.map { i => i.foreach(println) }.map { _ => ExitCode.Success }

        // program5.map { i => i.foreach(println) }.map { _ => ExitCode.Success }

        // program6(xa).map{ _ => ExitCode.Success }

        // program7(xa, 150000000).map{_ => ExitCode.Success }

        {
            val y = xa.yolo; import y._
            // query8(150000000 to 200000000).quick
            // query9(100000000 to 300000000, NonEmptyList.of("USA", "BRA", "PAK", "GBR")).quick
            // query10(NonEmptyList.of("IRN", "JPN", "USA", "GBR")).quick
            // program11(xa)

            // biggerThan(0).check // WOW
            // or
            // biggerThan(0).checkOutput
            /*
              Query0[LearnDoobie.Country] defined at LearnDoobie.scala:130
              SELECT code, name, population, gnp, indepyear
              FROM country
              WHERE population > ?
              ✓ SQL Compiles and TypeChecks
              ✕ P01 Short  →  INTEGER (int4)
                Short is not coercible to INTEGER (int4) according to the JDBC
                specification. Expected schema type was SMALLINT.
              ✓ C01 code       CHAR     (bpchar)  NOT NULL  →  String
              ✓ C02 name       VARCHAR  (varchar) NOT NULL  →  String
              ✓ C03 population INTEGER  (int4)    NOT NULL  →  Int
              ✕ C04 gnp        NUMERIC  (numeric) NULL      →  Option[Double]
                NUMERIC (numeric) is ostensibly coercible to Option[Double]
                according to the JDBC specification but is not a recommended
                target type. Expected schema type was FLOAT or DOUBLE.
              ✕ C05 indepyear  SMALLINT (int2)    NULL      →
                Column is unused. Remove it from the SELECT statement.
            */
            // The check logic requires both a database connection and concrete Get and Put instances
            // that define column-level JDBC mappings.


            // dataDef(xa).map{ i => println(i) }

            // insert("Reza", 30.toShort.some).run.transact(xa)

            // update("Reza", 31).quick
            // insert2("Majid", 29.toShort.some).quick

            // insert3("Hussein", 31.toShort.some).quick

            // up.quick

            // byName("U%")

            arrayTest(xa).map { i => println(i) }

        }.map{_ => ExitCode.Success }
    }


}

/*

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import scala.concurrent.ExecutionContext

// We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
// is where nonblocking operations will be executed.
implicit val cs = IO.contextShift(ExecutionContext.global)

// A transactor that gets connections from java.sql.DriverManager and excutes blocking operations
// on an unbounded pool of daemon threads. See the chapter on connection handling for more info.
val xa = Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql://192.168.1.106:5432/world",
        "testuser",
        "testpass"
    )
val y = xa.yolo
import y._

*/
