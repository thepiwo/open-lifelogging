package de.thepiwo.lifelogging.restapi.models

import com.github.t3hnar.bcrypt._

case class UserEntity(id: Option[Long] = None, username: String, password: String) {
  require(!username.isEmpty, "username.empty")
  require(!password.isEmpty, "password.empty")

  def public: PublicUserEntity = {
    PublicUserEntity(id, username)
  }
}

case class PublicUserEntity(id: Option[Long] = None, username: String)

case class UserEntityUpdate(username: Option[String] = None, password: Option[String] = None) {
  def merge(user: UserEntity): UserEntity = {
    val encryptedPassword = password.flatMap { password => Some(password.bcrypt) }
    UserEntity(user.id, username.getOrElse(user.username), encryptedPassword.getOrElse(user.password))
  }
}