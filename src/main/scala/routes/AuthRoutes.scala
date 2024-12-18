package routes

import cats.effect.IO
import domain.repositories.User
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.dsl.io.*
import org.http4s.headers.Location
import org.postgresql.util.PSQLException
import repositories.user.UserRepository
import services.OAuthService

class AuthRoutes(oauthClient: OAuthService, userRepository: UserRepository[IO]) {
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userEntityEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      for {
        authUrl  <- oauthClient.getAuthorizationUrl
        response <- TemporaryRedirect(Location(authUrl))
      } yield response

    case req @ POST -> Root =>
      val responseError = for {
        code     <- req.as[String]
        token    <- oauthClient.exchangeCodeForToken(code)
        user     <- oauthClient.getUserInfo(token)
        _        <- createOrUpdateUser(user)
        response <- Ok(user)
      } yield response

      responseError.handleErrorWith {
        case e: OAuthService.TokenException    => BadRequest(s"Failed to acquire token with provided code")
        case e: OAuthService.UserInfoException => InternalServerError("Failed to get user information")
      }

  }

  private def createOrUpdateUser(user: User): IO[Unit] =
    userRepository.createUser(user).handleErrorWith { case e: PSQLException => userRepository.updateUser(user) }
}
