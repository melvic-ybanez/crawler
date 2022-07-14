package com.melvic.crawler

import io.circe.parser._
import zhttp.http._
import zhttp.service.Server
import zio._

object Application extends App {
  val api: HttpApp[ZEnv, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "api" / "crawl" =>
      req.bodyAsString.flatMap { body =>
        decode[Input](body) match {
          case Left(error)        =>
            ZIO.succeed(Response.text(error.getMessage).setStatus(Status.BAD_REQUEST))
          case Right(Input(urls)) => Crawler.crawlUrls(urls)
        }
      }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, api).exitCode
}
