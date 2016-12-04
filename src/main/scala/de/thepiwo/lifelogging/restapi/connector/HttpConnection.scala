package de.thepiwo.lifelogging.restapi.connector

import com.typesafe.scalalogging.Logger
import spray.http._

object HttpConnection {

  val log: Logger = Logger[HttpConnection.type]

  val logReq: HttpRequest => HttpRequest = { r => log.debug(r.toString); r }
  val logResp: HttpResponse => HttpResponse = { r => log.debug(r.toString); r }


  def setContentType(mediaType: MediaType)(r: HttpResponse): HttpResponse = {
    r.withEntity(HttpEntity(ContentType(mediaType), r.entity.data))
  }

}