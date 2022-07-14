package com.melvic.crawler

import org.jsoup.Jsoup
import zhttp.http.Response
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.ZIO

object Crawler {
  type ZRecordData = Program[List[(String, String)]]

  def fetchLinks(content: String)(implicit env: Env): List[String] = {
    val doc  = Jsoup.parse(content)
    val link = doc.select("a")
    (0 until link.size()).foldLeft(List.empty[String]) { (urls, i) =>
      val url = link.get(i).attr("href")
      if (env.isNew(url)) url :: urls
      else urls
    }
  }

  def fetchData(url: String)(implicit env: Env): Program[(String, List[String])] = for {
    res  <- Client.request(url)
    data <- res.bodyAsString
  } yield (data, Crawler.fetchLinks(data))

  def crawl(env: Env): ZRecordData = {
    def recurse(
        visited: List[String],
        unvisited: Program[List[String]],
        acc: ZRecordData,
      ): ZRecordData =
      unvisited.flatMap {
        case Nil                    => acc
        case url :: restOfUnvisited =>
          val newVisited           = url :: visited
          implicit val newEnv: Env = Env(newVisited, restOfUnvisited)
          val result               = Crawler.fetchData(url)
          val newAcc = acc.flatMap(xs => result.map { case (data, _) => (data, url) :: xs })
          recurse(newVisited, result.map { case (_, urls) => restOfUnvisited ++ urls }, newAcc)
      }
    recurse(env.visited, ZIO.succeed(env.unvisited), ZIO.succeed(Nil))
  }

  def crawlUrls(urls: List[String]): ZIO[zio.ZEnv, Throwable, Response] = {
    val env          = ChannelFactory.auto ++ EventLoopGroup.auto()
    val outputEffect =
      Output.toJson(Output.fromRecordData(crawl(Env(Nil, urls)))).provideCustomLayer(env)
    outputEffect.map(Response.json)
  }
}
