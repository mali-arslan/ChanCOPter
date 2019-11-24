import types._

import io.circe.syntax._
import cats.effect.IO
import ChanCOPter._
import cats.implicits._
import cats.data.EitherT

import scala.util.Try
object Main extends App {

  val exampleMarket =
    """
      |{
      |  "products": [
      |    {
      |      "id": "00019DEPAC",
      |      "name": "Basic",
      |      "price": 699,
      |      "channelIds": [
      |        11245,
      |        2024,
      |        10231,
      |        2025
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin

  val getBudget =
    EitherT {
      IO {
        Try { args(0).toInt }.toEither
      }
    }

  val program = for {
    products <- getProducts("market.json")
    budget <- getBudget.leftMap(t => ArgumentError(t.getMessage)).leftWiden[Failure]
    sortedProducts = products.sorted
    productsToBuy = pickProducts(sortedProducts, budget)
    reportJson = Report(productsToBuy).asJson
  } yield reportJson

  program.value.unsafeRunSync() match {
    case Right(report) => println(report.spaces2)
    case Left(_: ArgumentError) =>
      println("Invalid argument(s)\n Usage: ./ChanCOPter <budget in cents>")
    case Left(_: JsonFailure) =>
      println(
        "Market JSON file is not found or is invalid. Please make sure that market.json file is in the same directory as ChanCOPter and adheres to the following example: \n" + exampleMarket)
  }
}
