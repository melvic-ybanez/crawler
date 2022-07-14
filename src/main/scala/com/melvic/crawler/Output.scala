package com.melvic.crawler

import com.melvic.crawler.Output.Record
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class Output(result: List[Record], error: Option[String])

object Output {
  final case class Record(url: String, data: String)

  implicit val resultItemDecoder: Decoder[Record] = deriveDecoder[Record]
  implicit val outputDecoder: Decoder[Output]     = deriveDecoder[Output]

  def fromResults(results: List[Record]): Output =
    Output(result = results, error = None)

  def error(errorMessage: String): Output =
    Output(Nil, error = Some(errorMessage))

  def fromLinksEffect(linksEffect: Program[List[(String, String)]]): Program[Output] =
    linksEffect.either.map {
      case Right(links) => fromResults(links.map { case (data, url) => Record(data, url) })
      case Left(error)  => Output.error(error.getMessage)
    }
}
