package de.thepiwo.lifelogging.restapi.models

import java.sql.Timestamp


case class LogEntity(id: Option[Long] = None,
                     userId: Option[Long],
                     key: String,
                     createdAt: Timestamp)

case class LogEntityReturn(id: Option[Long],
                           userId: Option[Long],
                           key: String,
                           logCoordEntity: Option[LogCoordEntity],
                           createdAt: Timestamp)

case class LogEntityInsert(logCoordEntity: Option[LogCoordEntity])

case class LogCoordEntity(id: Option[Long] = None,
                          logEntityId: Option[Long],
                          latitude: Double,
                          longitude: Double)



