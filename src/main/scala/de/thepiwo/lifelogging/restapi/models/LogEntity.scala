package de.thepiwo.lifelogging.restapi.models

import java.sql.Timestamp


case class LogEntity(id: Option[Long] = None,
                     userId: Option[Long],
                     key: String,
                     createdAt: Timestamp)

case class LogEntityReturn(id: Option[Long],
                           userId: Option[Long],
                           key: String,
                           logEntity: LogEntityInsert,
                           createdAt: Timestamp)

case class LogEntityInsert(logEntities: LogEntities)




