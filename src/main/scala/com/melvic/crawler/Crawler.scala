package com.melvic.crawler

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import zhttp.http._
import zhttp.service.Server
import zio._

object Crawler extends App {
  final case class RequestBody(urls: List[String])

  implicit val requestBodyDecoder: Decoder[RequestBody] = deriveDecoder[RequestBody]

  val api: HttpApp[Any, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "api" / "crawl" =>
      req.bodyAsString.map { body =>
        decode[RequestBody](body) match {
          case Left(error) => Response.text(error.getMessage).setStatus(Status.BAD_REQUEST)
          case Right(body) => Response.text(body.urls.mkString(" "))
        }
      }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, api).exitCode
}
