import types._

import scala.io.Source
import io.circe.parser._
import io.circe.syntax._
import cats.effect.IO

object Main extends App {

  val program = for {
    budget <- IO(args(0).toInt)
    products <- getProducts("market.json")
    sortedProducts = products.sorted
    productsToBuy = pickProducts(sortedProducts, budget)
    reportJson <- IO(Report(productsToBuy).asJson)
  } yield reportJson

  def getProducts(filePath: String): IO[List[Product]] = IO {
    val marketJson = Source.fromFile(filePath).mkString

    val maybeMarket = for {
      json <- parse(marketJson)
      market <- json.as[Market]
    } yield market

    maybeMarket match {
      case Left(e)       => throw new IllegalStateException("Broken market: " + e)
      case Right(market) => market.products
    }

  }
  def pickProducts(products: List[Product], budget: Int): List[Product] =
    products //???

  println(program.unsafeRunSync().spaces2)
}
