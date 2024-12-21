package services

import cats.effect.IO
import domain.repositories.{Message, User}
import org.http4s.{EntityDecoder, Request}
import repositories.message.MessageRepository
import repositories.user.UserRepository
import services.MessengerService.{InvalidTokenException, InvalidUserException, JSONParseException}

class MessengerService(userRepository: UserRepository[IO], messageRepository: MessageRepository[IO]) {
  def parseUserData[A](request: Request[IO])(implicit decoder: EntityDecoder[IO, A]): IO[A] = request
    .as[A]
    .handleErrorWith(_ => IO.raiseError(JSONParseException("Failed to parse JSON")))

  def validateAndGetUser(login: String): IO[User] =
    for {
      userOption <- userRepository.getUserByLogin(login)
      user       <- IO.fromOption(userOption)(InvalidUserException(s"User $login not found"))
    } yield user

  def validateToken(user: User, token: String): IO[Unit] =
    if (user.oauthToken != token) IO.raiseError(InvalidTokenException("Invalid token provided"))
    else IO(())

  def getUnreadMessages(sender: String, recipient: String): IO[List[String]] =
    for {
      messages <- messageRepository.getUnreadMessagesFrom(sender, recipient)
      _        <- messageRepository.deleteUnreadMessages(sender, recipient)
    } yield messages

  def sendMessage(sender: String, recipient: String, content: String): IO[Unit] =
    for {
      message <- IO(Message(sender, recipient, content))
      _       <- messageRepository.saveMessage(message)
    } yield ()
}

object MessengerService {
  class InvalidUserException(message: String) extends RuntimeException(message)

  class InvalidTokenException(message: String) extends RuntimeException(message)

  class JSONParseException(message: String) extends RuntimeException(message)
}
