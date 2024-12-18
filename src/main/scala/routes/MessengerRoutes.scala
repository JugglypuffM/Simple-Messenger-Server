package routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.io.*
import services.MessengerService
import services.MessengerService.{InvalidTokenException, InvalidUserException, JSONParseException}

class MessengerRoutes(messengerService: MessengerService) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root =>
      val responseError = for {
        (userLogin, token, senderLogin) <- messengerService.parseUserData[(String, String, String)](req)
        user                            <- messengerService.validateAndGetUser(userLogin)
        sender                          <- messengerService.validateAndGetUser(senderLogin)
        _                               <- messengerService.validateToken(user, token)
        messages                        <- messengerService.getUnreadMessages(sender.login, user.login)
        response                        <- Ok(messages)
      } yield response

      responseError.handleErrorWith {
        case _: InvalidUserException  => BadRequest("User not found")
        case _: InvalidTokenException => BadRequest("Invalid token provided")
        case _: JSONParseException    => BadRequest("Invalid JSON")
      }

    case req @ POST -> Root =>
      val responseError = for {
        (login, token, recipient, message) <- messengerService.parseUserData[(String, String, String, String)](req)
        sender                             <- messengerService.validateAndGetUser(login)
        recipient                          <- messengerService.validateAndGetUser(recipient)
        _                                  <- messengerService.validateToken(sender, token)
        _                                  <- messengerService.sendMessage(sender.login, recipient.login, message)
        response                           <- Ok("Message sent successfully")
      } yield response

      responseError.handleErrorWith {
        case _: InvalidUserException  => BadRequest("User or recipient not found")
        case _: InvalidTokenException => BadRequest("Invalid token provided")
        case _: JSONParseException    => BadRequest("Invalid JSON")
      }
  }
}
