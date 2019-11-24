import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ChanCOPter._
import cats.data.EitherT
import cats.effect.IO
import org.scalactic.source
import org.scalatest.compatible.Assertion
import org.scalatest.prop.Checkers
import org.scalatest.words.ResultOfStringPassedToVerb
import types._
import org.scalacheck.{Gen, Prop}
import types.Product._

class ChanCOPterTest
    extends FlatSpec
    with Matchers
    with EitherValues
    with Checkers {
  import ChanCOPterTest._
  implicit class ItVerbStringExtension(test: ItVerbString) {
    def inEitherTIO(op: => EitherT[IO, Failure, Assertion])(
        implicit pos: source.Position): Unit =
      test.in(op.value.unsafeRunSync())(pos)
  }

  implicit class StringVerbStringInvocation(test: ResultOfStringPassedToVerb) {
    def inEitherTIO(op: => EitherT[IO, Failure, Assertion])(
        implicit pos: source.Position): Unit =
      test.in(op.value.unsafeRunSync())(pos)
  }

  "getProducts" should "get the list of products from valid json with no products" inEitherTIO {
    val path = getClass.getResource("/validMarket0Product.json").getPath
    for {
      list <- getProducts(path)
    } yield list shouldBe empty
  }

  it should "get the list of products from valid json" inEitherTIO {
    val path = getClass.getResource("/validMarket1Product.json").getPath
    for {
      list <- getProducts(path)
    } yield list.length shouldBe 1
  }

  it should "get the list of products from valid json with 2 products" inEitherTIO {
    val path = getClass.getResource("/validMarket2Products.json").getPath
    for {
      list <- getProducts(path)
    } yield list.length shouldBe 2
  }

  it should "return a Left when the file is invalid" inEitherTIO {
    val path = getClass.getResource("/invalidMarket1Product.json").getPath
    for {
      list <- getProducts(path)
    } yield list should be('left)
  }

  "pickProducts" should "pick validly according to the budget" in {
    check {
      Prop.forAll(Gen.listOfN(100, productGen), Gen.choose(500, 20000)) { (products, budget) =>
        val sortedProducts = products.sorted
        val pickedProducts = pickProducts(sortedProducts, budget)
        // don't exceed budget
        pickedProducts.map(_.price.value).sum <= budget &&
        // don't have duplicate products
        pickedProducts.distinct.length == pickedProducts.length
      }
    }
  }

  it should "pick the most #chans/price products it can for the budget" in {
    check {
      Prop.forAllNoShrink(Gen.listOfN(1000, productGen), Gen.choose(500, 20000)) { (products, budget) =>
        val sortedProducts = products.sorted.reverse
        val pickedProducts = pickProducts(products, budget)
        // short circuit if nothing's picked
        if (pickedProducts.isEmpty) {
          // should pick something when possible
          !sortedProducts.exists(_.price.value <= budget)
        }
        else {
          val unpickedProducts = sortedProducts.filterNot(pickedProducts.contains(_))
          val budgetLeft = budget - pickedProducts.map(_.price.value).sum
          val mostValuablePicked = pickedProducts.head
          // should buy as much as possible
          !unpickedProducts.exists(_.price.value <= budgetLeft) &&
            // don't miss more valuable products
            unpickedProducts.forall { u =>
              u.price.value >= budget || implicitly[Ordering[Product]].lteq(u, mostValuablePicked)
            }
        }
      }
    }
  }
}

object ChanCOPterTest {
  val productGen: Gen[Product] =
    for {
      id <- Gen.pick(12, ('0' to '9') ++ ('A' to 'Z'))
      name <- Gen.pick(5, 'a' to 'z')
      price <- Gen.choose(100, 10000)
      nChans <- Gen.choose(1, 10)
      channelIds <- Gen.listOfN(nChans, Gen.choose(1, 10))
    } yield
      Product(ProductId(id.toString()),
              ProductName(name.toString()),
              Price(price),
              channelIds.map(ChannelId(_)))

}
