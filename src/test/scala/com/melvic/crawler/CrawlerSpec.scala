package com.melvic.crawler

import zio.test._
import zio.test.Assertion._
import zhttp.http._

object CrawlerSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("""CrawlerSpec""")(
    testM("200 ok") {
      checkAllM(Gen.fromIterable(List("text", "json"))) { uri =>
        val request = Request(Method.GET, URL(!! / uri))
        assertM(Crawler.api(request).map(_.status))(equalTo(Status.OK))
      }
    },
  )
}
