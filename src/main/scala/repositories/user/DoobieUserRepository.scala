package repositories.user

import cats.effect.*
import domain.repositories.User
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor

class DoobieUserRepository[F[_]: Async](xa: Transactor[F]) extends UserRepository[F] {
  override def initialize(): F[Unit] =
    sql"""
      CREATE TABLE IF NOT EXISTS users (
        login VARCHAR PRIMARY KEY,
        uname VARCHAR NOT NULL,
        token VARCHAR UNIQUE NOT NULL
      )
      """.update.run.map(_ => ()).transact(xa)

  override def updateUser(user: User): F[Unit] = {
    sql"""
      UPDATE users
      SET login = ${user.login},
          uname = ${user.name},
          token = ${user.oauthToken}
      WHERE login = ${user.login}
       """.update.run.map(_ => ()).transact(xa)
  }

  override def getUserByLogin(login: String): F[Option[User]] = {
    sql"""
      SELECT login, uname, token
      FROM users
      WHERE login = $login
      """.query[User].option.transact(xa)
  }

  def persistUser(user: User): F[Unit] =
    createUser(user)

  override def createUser(user: User): F[Unit] = {
    sql"""
      INSERT INTO users (login, uname, token)
      VALUES (${user.login}, ${user.name}, ${user.oauthToken})
      """.update.run.map(_ => ()).transact(xa)
  }
}
