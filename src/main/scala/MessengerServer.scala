import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.*
import domain.services.OAuthConfig.yandexOAuthConfig
import doobie.Transactor
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.server.Router
import repositories.message.DoobieMessageRepository
import repositories.user.DoobieUserRepository
import routes.{AuthRoutes, MessengerRoutes}
import services.{MessengerService, OAuthService}

object MessengerServer extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      transactor <- initTransactor()

      userRepository    <- DoobieUserRepository[IO](transactor).pure[IO]
      messageRepository <- DoobieMessageRepository[IO](transactor).pure[IO]

      _ <- userRepository.initialize()
      _ <- messageRepository.initialize()

      router <- Router(
        "/login"     -> AuthRoutes(OAuthService(yandexOAuthConfig), userRepository).routes,
        "/messenger" -> MessengerRoutes(MessengerService(userRepository, messageRepository)).routes
      ).orNotFound.pure[IO]

      _ <- BlazeServerBuilder[IO]
        .bindHttp(8081, "192.168.1.74")
        .withHttpApp(router)
        .serve
        .compile
        .drain
    } yield ExitCode.Success

  private def initTransactor(): IO[Transactor[IO]] = Transactor
    .fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = "jdbc:postgresql://localhost:8080/postgres",
      user = "postgres",
      password = "grespost",
      logHandler = None
    )
    .pure[IO]
}
