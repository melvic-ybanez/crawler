package com.melvic

import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.ZIO

package object crawler {
  type Program[A] = ZIO[EventLoopGroup with ChannelFactory, Throwable, A]
}
