package domain.services

import org.http4s.Uri

case class OAuthConfig(
    clientId: String,
    clientSecret: String,
    authUri: Uri,
    tokenUri: Uri,
    redirectUri: Uri,
    infoUri: Uri
)

object OAuthConfig {
  val yandexOAuthConfig: OAuthConfig = OAuthConfig(
    clientId = "8bdeccb4b70b4afbb3fc35fe9d4069dc",
    clientSecret = "16dad29068804ed38daccce42c3cf921",
    authUri = Uri.unsafeFromString("https://oauth.yandex.ru/authorize"),
    tokenUri = Uri.unsafeFromString("https://oauth.yandex.ru/token"),
    redirectUri = Uri.unsafeFromString("https://oauth.yandex.ru/verification_code"),
    infoUri = Uri.unsafeFromString("https://login.yandex.ru/info")
  )
}
