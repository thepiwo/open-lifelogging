package de.thepiwo.lifelogging.restapi.models

import java.util.UUID

case class TokenEntity(id: Long = 0,
                       userId: Long,
                       token: String = UUID.randomUUID().toString)
