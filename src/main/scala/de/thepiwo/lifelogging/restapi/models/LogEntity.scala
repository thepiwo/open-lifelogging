package de.thepiwo.lifelogging.restapi.models

import java.sql.Timestamp
import spray.json.JsValue


case class LogEntity(id: Option[Long],
                     userId: Option[Long],
                     key: String,
                     data: JsValue,
                     hash: String,
                     createdAtClient: Timestamp,
                     createdAt: Timestamp)

case class LogEntityInsert(key: String,
                           data: JsValue,
                           createdAtClient: Long)



