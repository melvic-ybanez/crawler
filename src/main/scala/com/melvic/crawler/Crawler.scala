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
}
