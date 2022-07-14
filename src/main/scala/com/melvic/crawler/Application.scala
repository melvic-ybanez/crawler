package com.melvic.crawler

import io.circe.parser._
import zhttp.http._
import zhttp.service.{ChannelFactory, EventLoopGroup, Server}
import zio._

object Application extends App {
  val api: HttpApp[Any, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "api" / "crawl" =>
      req.bodyAsString.map { body =>
        decode[Input](body) match {
          case Left(error) => Response.text(error.getMessage).setStatus(Status.BAD_REQUEST)
          case Right(body) => Response.text(body.urls.mkString(" "))
        }
      }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, api).exitCode
}
