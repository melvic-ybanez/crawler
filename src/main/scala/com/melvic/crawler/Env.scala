package com.melvic.crawler

final case class Env(visited: List[String], unvisited: List[String]) {
  def isNew(url: String): Boolean =
    visited.contains(url) && unvisited.contains(url)
}
