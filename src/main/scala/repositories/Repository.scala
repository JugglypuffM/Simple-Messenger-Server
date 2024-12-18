package repositories

trait Repository[F[_]] {
  def initialize(): F[Unit]
}
