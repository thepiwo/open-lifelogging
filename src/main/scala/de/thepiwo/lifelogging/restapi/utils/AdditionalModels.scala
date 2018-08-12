package de.thepiwo.lifelogging.restapi.utils

import de.thepiwo.lifelogging.restapi.models.LogEntity

case class UnauthorizedException(msg: String) extends Exception(msg)

case class LoginPassword(login: String, password: String)

case class LogEntityReturn(countTotal: Int, logs: Seq[LogEntity])