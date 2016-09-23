package de.thepiwo.lifelogging.restapi.utils

import java.sql.Timestamp

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import de.thepiwo.lifelogging.restapi.models.{LogEntities, _}
import spray.json._


trait AdditionalJsonProtocol extends DefaultJsonProtocol {

  implicit object TimestampJsonFormat extends RootJsonFormat[Timestamp] {
    override def write(timestamp: Timestamp): JsString = JsString(timestamp.toString)

    override def read(value: JsValue): Timestamp = value match {
      case JsString(timestamp) => Timestamp.valueOf(timestamp)
      case _ => new Timestamp(0L)
    }
  }

  implicit val coordEntityFormat = jsonFormat6(CoordEntity)

  implicit val wifiEntityFormat = jsonFormat5(WifiEntity)

  implicit val logEntitiesFormat = new RootJsonFormat[LogEntities] {
    def write(obj: LogEntities): JsValue =
      (obj match {
        case c: CoordEntity => JsObject(obj.getClass.getSimpleName -> c.toJson, "type" -> JsString(obj.getClass.getSimpleName))
        case d: WifiEntity => JsObject(obj.getClass.getSimpleName -> d.toJson, "type" -> JsString(obj.getClass.getSimpleName))
      }).asJsObject

    def read(json: JsValue): LogEntities =
      json.asJsObject.getFields("type") match {
        case Seq(JsString("CoordEntity")) => json.asJsObject.getFields("CoordEntity").head.convertTo[CoordEntity]
        case Seq(JsString("WifiEntity")) => json.asJsObject.getFields("WifiEntity").head.convertTo[WifiEntity]
      }
  }


}

trait JsonProtocol extends SprayJsonSupport with AdditionalJsonProtocol with NullOptions {

  implicit val tokenEntityFormat = jsonFormat3(TokenEntity)

  implicit val loginPasswordFormat = jsonFormat2(LoginPassword)

  implicit val userEntityFormat = jsonFormat3(UserEntity)

  implicit val logEntityFormat = jsonFormat4(LogEntity)

  implicit val logEntityInsertFormat = jsonFormat1(LogEntityInsert)

  implicit val logEntityReturnFormat = jsonFormat5(LogEntityReturn)

  implicit val publicUserEntityFormat = jsonFormat2(PublicUserEntity)

  implicit val userEntityUpdateFormat = jsonFormat2(UserEntityUpdate)

}
