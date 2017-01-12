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

  implicit val tokenEntityFormat: RootJsonFormat[TokenEntity] = jsonFormat3(TokenEntity)
  implicit val loginPasswordFormat: RootJsonFormat[LoginPassword] = jsonFormat2(LoginPassword)
  implicit val userEntityFormat: RootJsonFormat[UserEntity] = jsonFormat3(UserEntity)
  implicit val logEntityFormat: RootJsonFormat[LogEntity] = jsonFormat7(LogEntity)
  implicit val logEntityFormatInsert: RootJsonFormat[LogEntityInsert] = jsonFormat3(LogEntityInsert)
  implicit val publicUserEntityFormat: RootJsonFormat[PublicUserEntity] = jsonFormat2(PublicUserEntity)
  implicit val userEntityUpdateFormat: RootJsonFormat[UserEntityUpdate] = jsonFormat2(UserEntityUpdate)

}

