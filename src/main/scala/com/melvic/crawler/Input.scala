package com.melvic.crawler

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class Input(urls: List[String])

object Input {
  implicit val inputDecoder: Decoder[Input] = deriveDecoder[Input]
}
