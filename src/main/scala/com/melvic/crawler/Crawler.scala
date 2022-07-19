package com.melvic.crawler

import org.jsoup.Jsoup
import zhttp.service.Client
import zio._

object Crawler {
  type StateRef = Ref[State]

  /**
   * An effectual computation over the state
   */
  type ZState = Program[State]

  def crawl(urls: Set[Url]): ZState = {
    def recurse(urls: Set[Url], stateRef: StateRef): ZState =
      ZIO
        .foreachParN(500)(urls.toList) { url =>
          getContent(url).flatMap { content =>
            val contentUrls = fetchUrls(content)
            stateRef.modify { state =>
              (contentUrls -- state.urls, state.addUrls(contentUrls).addEntry(url, content))
            }
          }.orElse(ZIO.succeed(Nil))
        }
        .map(_.toSet.flatten)
        .flatMap { urls =>
          if (urls.isEmpty) stateRef.get
          else recurse(urls, stateRef)
        }

    for {
      stateRef <- Ref.make(State.fromUrls(urls))
      _        <- recurse(urls, stateRef)
      state    <- stateRef.get
      _ = println(state.table.size)
    } yield state
  }

  def getContent(url: Url): Program[Content] =
    Client.request(url).flatMap(_.bodyAsString)

  def fetchUrls(content: Content): Set[Url] = {
    val doc  = Jsoup.parse(content)
    val link = doc.select("a")
    val urls = (0 until link.size()).foldLeft(List.empty[String]) { (urls, i) =>
      val url = link.get(i).attr("href")
      url :: urls
    }
    urls.toSet
  }
  /**
   * Crawls from a list of visited and unvisited links
   */
//  def crawl(env: Env): ZRecordData = {
//    def recurse(
//        visited: List[String],
//        unvisited: Program[List[String]],
//        acc: ZRecordData,
//      ): ZRecordData =
//      unvisited.flatMap {
//        case Nil                    => acc
//        case url :: restOfUnvisited =>
//          val newVisited           = url :: visited
//          implicit val newEnv: Env = Env(newVisited, restOfUnvisited)
//          val result               = Crawler.fetchData(url)
//          val newAcc               = acc.flatMap { xs =>
//            result
//              // update the accumulator...
//              .map { case (data, _) => (data, url) :: xs }
//              // ...unless the url can not be accessed
//              .orElse(acc)
//          }
//          val newUnvisited         = result
//            // update the unvisited list...
//            .map { case (_, urls) => restOfUnvisited ++ urls }
//            // ...unless the url can not be accessed
//            .orElse(ZIO.succeed(restOfUnvisited))
//          recurse(newVisited, newUnvisited, newAcc)
//      }
//    recurse(env.visited, ZIO.succeed(env.unvisited), ZIO.succeed(Nil))
//  }

//  def fetchUrls(content: String)(implicit env: Env): List[String] = {
//    val doc  = Jsoup.parse(content)
//    val link = doc.select("a")
//    (0 until link.size()).foldLeft(List.empty[String]) { (urls, i) =>
//      val url = link.get(i).attr("href")
//      if (env.isNew(url)) url :: urls
//      else urls
//    }
//  }

  /**
   * Fetches all the links from the given url, including the content of the web page (for output
   * purposes). The result is effectual.
   */
//  def fetchData(url: String)(implicit env: Env): Program[(Content, List[String])] = for {
//    res  <- Client.request(url)
//    data <- res.bodyAsString
//  } yield (data, Crawler.fetchUrls(data))
}
