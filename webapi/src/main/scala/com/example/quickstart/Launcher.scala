package com.example.quickstart

import doobie._, doobie.implicits._
import cats.effect._
import cats.implicits._

case class Country(code: String, name: String, population: Long, gnp: Double)

class CountryRepo(tableName: String) {

    val selectAll = fr"SELECT code, name, population, gnp FROM ${tableName}"

    def top(n: Int) =
        { selectAll ++ fr"LIMIT $n" }.query[Country].to[List]

    def top(n: Int, afterCode: String)= {
        selectAll ++
            fr"WHERE code > ${afterCode} LIMIT $n"
    }.query[Country].to[List]

    def byCode(code: String)=
        { selectAll ++ fr"WHERE code = $code" }.query[Country].unique

    def delete(code: String) =
        sql"DELETE FROM ${tableName} WHERE code = $code".update.run

    /*
    // Other databases (including PostgreSQL) provide a way to do this in
    // one shot by returning multiple specified columns from the inserted row.
    def insert3(name: String, age: Option[Short]): ConnectionIO[Person] = {
        sql"insert into person (name, age) values ($name, $age)"
            .update
            .withUniqueGeneratedKeys("id", "name", "age")
     */

}
