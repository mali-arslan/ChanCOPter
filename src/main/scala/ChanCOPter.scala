import cats.data.EitherT
import cats.effect.IO
import io.circe.parser.parse
import types.{Failure, JsonFailure, Market, Product}
import cats.implicits._

import scala.annotation.tailrec
import scala.io.Source

object ChanCOPter {

  def getProducts(filePath: String): EitherT[IO, Failure, List[Product]] =
    EitherT {
      IO {
        val marketJson = Source.fromFile(filePath).mkString

        val maybeMarket = for {
          json <- parse(marketJson)
          market <- json.as[Market]
        } yield market

        maybeMarket
          .leftMap(e => JsonFailure(e.getMessage))
          .leftWiden[Failure]
          .map(_.products)
      }
    }
  def pickProducts(products: List[Product], budget: Int): List[Product] = {
    @tailrec
    def recursivePicker(productsLeftToPick: List[Product],
                        budgetLeft: Int, pickedProducts: List[Product]): List[Product] =
      productsLeftToPick match {
        case head :: tail =>
          if (head.price.value <= budgetLeft)
            recursivePicker(tail, budgetLeft - head.price.value, head::pickedProducts)
          else
            recursivePicker(tail, budgetLeft, pickedProducts)
        case Nil => Nil
      }
    recursivePicker(products.sorted.reverse, budget, List())
  }

}
