package repositories.message

import cats.effect.kernel.Async
import domain.repositories.Message
import doobie.implicits.*
import doobie.util.transactor.Transactor

class DoobieMessageRepository[F[_]: Async](xa: Transactor[F]) extends MessageRepository[F] {
  override def initialize(): F[Unit] =
    sql"""
          CREATE TABLE IF NOT EXISTS messages (
            sender VARCHAR,
            recipient VARCHAR,
            content VARCHAR NOT NULL,
            CONSTRAINT FK_Sender FOREIGN KEY (sender) REFERENCES users(login),
            CONSTRAINT FK_Recipient FOREIGN KEY (recipient) REFERENCES users(login)
          )
          """.update.run.map(_ => ()).transact(xa)

  override def saveMessage(message: Message): F[Unit] =
    sql"""
      INSERT INTO messages (sender, recipient, content)
      VALUES (${message.sender}, ${message.recipient}, ${message.content})
      """.update.run.map(_ => ()).transact(xa)

  override def getUnreadMessagesFrom(sender: String, recipient: String): F[List[String]] =
    sql"""
      SELECT content 
      FROM messages 
      WHERE sender = $sender AND recipient = $recipient
      """.query[String].to[List].transact(xa)

  override def deleteUnreadMessages(recipient: String): F[Unit] =
    sql"""
      DELETE
      FROM messages 
      WHERE recipient = $recipient
      """.update.run.map(_ => ()).transact(xa)

}
