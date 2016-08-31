package de.thepiwo.lifelogging.restapi.utils

case class UnauthorizedException(msg: String) extends Exception(msg)

case class LoginPassword(login: String, password: String)

