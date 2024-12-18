package repositories.message

import domain.repositories.Message
import repositories.Repository

trait MessageRepository[F[_]] extends Repository[F] {
  def saveMessage(message: Message): F[Unit]
  def getUnreadMessagesFrom(sender: String, recipient: String): F[List[String]]
  def deleteUnreadMessages(recipient: String): F[Unit]
}
