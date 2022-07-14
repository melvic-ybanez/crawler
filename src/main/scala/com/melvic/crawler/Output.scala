package com.melvic.crawler

import com.melvic.crawler.Crawler.ZRecordData
import com.melvic.crawler.Output.Record
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

final case class Output(result: List[Record], error: Option[String])

object Output {
  final case class Record(url: String, data: String)

  implicit val recordDecoder: Decoder[Record] = deriveDecoder
  implicit val outputDecoder: Decoder[Output] = deriveDecoder

  implicit val recordEncoder: Encoder[Record] = deriveEncoder
  implicit val outputEncoder: Encoder[Output] = deriveEncoder

  def fromResults(results: List[Record]): Output =
    Output(result = results, error = None)

  def error(errorMessage: String): Output =
    Output(Nil, error = Some(errorMessage))

  def fromRecordData(recordData: ZRecordData): Program[Output] =
    recordData.either.map {
      case Right(links) => fromResults(links.map { case (data, url) => Record(url, data) })
      case Left(error)  => Output.error(error.getMessage)
    }

  def toJson(program: Program[Output]): Program[String] =
    program.map(_.asJson.noSpaces)
}
