package com.melvic

import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.ZIO

package object crawler {
  type Url = String
  type Content = String
  type Program[A] = ZIO[EventLoopGroup with ChannelFactory, Throwable, A]
}
