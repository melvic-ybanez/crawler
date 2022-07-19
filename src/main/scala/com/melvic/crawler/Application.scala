package com.melvic.crawler

import com.melvic.crawler.Crawler.crawl
import io.circe.parser._
import zhttp.http._
import zhttp.service.{ChannelFactory, EventLoopGroup, Server}
import zio._

object Application extends App {
  val api: HttpApp[ZEnv, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "api" / "crawl" =>
      req.bodyAsString.flatMap { body =>
        decode[Input](body) match {
          case Left(error)        =>
            ZIO.succeed(Response.text(error.getMessage).setStatus(Status.BAD_REQUEST))
          case Right(Input(urls)) => crawlUrls(urls)
        }
      }
  }

  def crawlUrls(urls: List[String]): ZIO[zio.ZEnv, Throwable, Response] = {
    val zEnv         = ChannelFactory.auto ++ EventLoopGroup.auto()
    val outputEffect =
      Output.toJson(Output.fromZState(crawl(urls.toSet))).provideCustomLayer(zEnv)
    outputEffect.map(Response.json)
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, api).exitCode
}
