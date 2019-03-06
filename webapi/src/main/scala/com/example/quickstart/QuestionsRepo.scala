package com.example.quickstart

// import cats._, cats.data._, cats.implicits._,
import cats.effect._
// import doobie._, doobie.implicits._

object data {

    type QuestionPackID = Long
    type QuestionID = Long
    type QuestionKindNum = Int

    sealed trait QuestionKind {}
    object QuestionKind {
        case object MutliOptionSingleSelect extends QuestionKind
        case object MutliOptionMultiSelect extends QuestionKind
    }

    case class Question(
        pack: QuestionID, identity: QuestionID,
        title: String, text: String, kind: QuestionKindNum
    )

}

trait QuestionsRepo[F[_] <: Effect[F]] {

    def put(question: data.Question): F[Long]
    def load(id: data.QuestionID): F[Option[data.Question]]
    def loadPack(pack: data.QuestionPackID): F[List[data.Question]]

}
