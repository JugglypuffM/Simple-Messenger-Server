package services

import cats.effect.*
import domain.repositories.User
import domain.services.OAuthConfig
import io.circe.Json
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.*
import org.http4s.client.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.*
import org.typelevel.ci.CIString
import services.OAuthService.{TokenException, UserInfoException}

class OAuthService(config: OAuthConfig) {

  /** Генерация URL для авторизации */
  def getAuthorizationUrl: IO[Uri] = IO {
    config.authUri.withQueryParams(
      Map(
        "response_type" -> "code",
        "client_id"     -> config.clientId,
        "redirect_uri"  -> config.redirectUri.renderString
      )
    )
  }

  /** Обмен кода авторизации на токен */
  def exchangeCodeForToken(authCode: String): IO[String] = {
    val request = POST(
      UrlForm(
        "grant_type"    -> "authorization_code",
        "code"          -> authCode,
        "client_id"     -> config.clientId,
        "client_secret" -> config.clientSecret
      ),
      config.tokenUri
    )
    BlazeClientBuilder[IO].resource
      .use { client =>
        client.expect[Json](request).map { json =>
          json.hcursor
            .get[String]("access_token")
            .getOrElse(
              throw TokenException("Failed to retrieve access_token")
            )
        }
      }
      .handleErrorWith(_ => IO.raiseError(TokenException("Failed to retrieve access_token")))
  }

  /** Получение информации о пользователе */
  def getUserInfo(token: String): IO[User] = {
    val request = GET(
      config.infoUri,
      Headers(Header.Raw(CIString("Authorization"), s"OAuth $token"))
    )
    BlazeClientBuilder[IO].resource
      .use { client =>
        client.expect[Json](request).map { json =>
          val cursor = json.hcursor
          val userResult =
            for {
              login <- cursor.get[String]("login")
              name  <- cursor.get[String]("display_name")
            } yield User(login, name, token)

          userResult.getOrElse(throw UserInfoException("Authentication failed"))
        }
      }
      .handleErrorWith(_ => IO.raiseError(UserInfoException("Authentication failed")))
  }
}

object OAuthService {
  class TokenException(message: String) extends RuntimeException(message)

  class UserInfoException(message: String) extends RuntimeException(message)
}
