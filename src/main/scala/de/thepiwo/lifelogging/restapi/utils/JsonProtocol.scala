package de.thepiwo.lifelogging.restapi.utils

import java.sql.Timestamp

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import de.thepiwo.lifelogging.restapi.models._
import spray.json._


trait AdditionalJsonProtocol extends DefaultJsonProtocol {

  implicit object TimestampJsonFormat extends RootJsonFormat[Timestamp] {
    override def write(timestamp: Timestamp): JsString = JsString(timestamp.toString)

    override def read(value: JsValue): Timestamp = value match {
      case JsString(timestamp) => Timestamp.valueOf(timestamp)
      case _ => new Timestamp(0L)
    }
  }

}

trait JsonProtocol extends SprayJsonSupport with AdditionalJsonProtocol with NullOptions {

  implicit val tokenEntityFormat = jsonFormat3(TokenEntity)

  implicit val loginPasswordFormat = jsonFormat2(LoginPassword)

  implicit val userEntityFormat = jsonFormat3(UserEntity)

  implicit val logEntityFormat = jsonFormat5(LogEntity)

  implicit val logEntityFormatInsert = jsonFormat2(LogEntityInsert)

  implicit val publicUserEntityFormat = jsonFormat2(PublicUserEntity)

  implicit val userEntityUpdateFormat = jsonFormat2(UserEntityUpdate)

}
