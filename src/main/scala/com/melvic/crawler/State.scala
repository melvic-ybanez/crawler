package com.melvic.crawler

import com.melvic.crawler.State.fromUrls

final case class State(table: Map[Url, Option[Content]]) {
  def urls: Set[Url] = table.keySet

  def addUrls(urls: Set[Url]): State =
    copy(table ++ fromUrls(urls).table)

  def addEntry(url: Url, content: Content): State =
    copy(table = table + (url -> Some(content)))
}

object State {
  def fromUrls(urls: Set[Url]): State =
    State(urls.map(url => (url, None)).toMap)
}
