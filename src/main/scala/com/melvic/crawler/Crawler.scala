package com.melvic.crawler

import org.jsoup.Jsoup
import zhttp.service.Client
import zio.ZIO

object Crawler {

  /**
   * An effectual computation over a list of record data
   */
  type ZRecordData = Program[List[(String, String)]]

  /**
   * Crawls from a list of visited and unvisited links
   */
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

  def fetchLinks(content: String)(implicit env: Env): List[String] = {
    val doc  = Jsoup.parse(content)
    val link = doc.select("a")
    (0 until link.size()).foldLeft(List.empty[String]) { (urls, i) =>
      val url = link.get(i).attr("href")
      if (env.isNew(url)) url :: urls
      else urls
    }
  }

  /**
   * Fetches all the links from the given url, including the content of the web page (for output
   * purposes). The result is effectual.
   */
  def fetchData(url: String)(implicit env: Env): Program[(String, List[String])] = for {
    res  <- Client.request(url)
    data <- res.bodyAsString
  } yield (data, Crawler.fetchLinks(data))
}
