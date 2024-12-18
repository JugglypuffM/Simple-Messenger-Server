package repositories.user

import domain.repositories.User
import repositories.Repository

trait UserRepository[F[_]] extends Repository[F] {
  def createUser(user: User): F[Unit]
  def updateUser(user: User): F[Unit]
  def getUserByLogin(userId: String): F[Option[User]]
}
