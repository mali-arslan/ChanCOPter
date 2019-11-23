import cats.data.EitherT
import cats.effect.IO
import io.circe.parser.parse
import types.{Failure, JsonFailure, Market, Product}
import cats.implicits._

import scala.io.Source


object ChanCOPter {

  def getProducts(filePath: String): EitherT[IO, Failure, List[Product]] = EitherT {
    IO {
      val marketJson = Source.fromFile(filePath).mkString

      val maybeMarket = for {
        json <- parse(marketJson)
        market <- json.as[Market]
      } yield market

      maybeMarket match {
        case Left(e) => JsonFailure(e.getMessage)
        case Right(market) => market.products
      }

      maybeMarket.leftMap(e => JsonFailure(e.getMessage)).leftWiden[Failure].map(_.products)
    }
  }
  def pickProducts(products: List[Product], budget: Int): List[Product] =
    products //???
}
