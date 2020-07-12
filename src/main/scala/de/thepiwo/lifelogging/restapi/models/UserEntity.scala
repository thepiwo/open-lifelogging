package de.thepiwo.lifelogging.restapi.models

import com.github.t3hnar.bcrypt._


case class SignUpUser(username: String,
                      password: String) {

  require(!username.isEmpty, "username.empty")
  require(!password.isEmpty, "password.empty")
}

case class UserEntity(id: Long = 0, username: String, password: String) {

  def public: PublicUserEntity = {
    PublicUserEntity(id, username)
  }
}


case class PublicUserEntity(id: Long,
                            username: String)

case class UserEntityUpdate(username: Option[String] = None,
                            password: Option[String] = None) {

  def merge(user: UserEntity): UserEntity = {
    val encryptedPassword = password.flatMap { password => Some(password.bcrypt) }
    UserEntity(user.id, username.getOrElse(user.username), encryptedPassword.getOrElse(user.password))
  }
}

case class UserSettingsEntity(id: Long = 0,
                              userId: Long,
                              lastFmUserName: Option[String])