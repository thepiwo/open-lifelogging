package de.thepiwo.lifelogging.restapi.models

import java.sql.Timestamp
import spray.json.JsValue


case class LogEntity(id: Long = 0,
                     userId: Long,
                     key: String,
                     data: JsValue,
                     hidden: Boolean = false,
                     createdAtClient: Timestamp,
                     createdAt: Timestamp)

case class LogEntityInsert(key: String,
                           data: JsValue,
                           createdAtClient: Long)



