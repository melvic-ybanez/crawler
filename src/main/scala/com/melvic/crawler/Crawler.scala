package com.melvic.crawler

import com.melvic.crawler.Crawler.ZRecordSet
import org.jsoup.Jsoup
import zhttp.service.Client
import zio.ZIO

final case class Crawler(env: Env) {
  def crawl: ZRecordSet = {
    def recurse(
        visited: List[String],
        unvisited: Program[List[String]],
        acc: ZRecordSet,
      ): ZRecordSet =
      unvisited.flatMap {
        case Nil                    => acc
        case url :: restOfUnvisited =>
          val newVisited           = url :: visited
          implicit val newEnv: Env = Env(newVisited, restOfUnvisited)
          val result               = fetchData(url)
          val newAcc = acc.flatMap(xs => result.map { case (data, _) => (data, url) :: xs })
          recurse(newVisited, result.map { case (_, urls) => restOfUnvisited ++ urls }, newAcc)
      }
    recurse(env.visited, ZIO.succeed(env.unvisited), ZIO.succeed(Nil))
  }

  def fetchData(url: String)(implicit env: Env): Program[(String, List[String])] = for {
    res  <- Client.request(url)
    data <- res.bodyAsString
  } yield (data, Crawler.fetchLinks(data))
}

object Crawler {
  type ZRecordSet = Program[List[(String, String)]]

  def fetchLinks(content: String)(implicit env: Env): List[String] = {
    val doc  = Jsoup.parse(content)
    val link = doc.select("a")
    (0 until link.size()).foldLeft(List.empty[String]) { (urls, i) =>
      val url = link.get(i).attr("href")
      if (env.isNew(url)) url :: urls
      else urls
    }
  }
}
