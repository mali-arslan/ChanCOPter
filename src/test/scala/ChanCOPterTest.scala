import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ChanCOPter._
import cats.data.EitherT
import cats.data.Ior.Left
import cats.effect.IO
import org.scalactic.source
import org.scalatest.compatible.Assertion
import org.scalatest.words.ResultOfStringPassedToVerb
import types.Failure
class ChanCOPterTest extends FlatSpec with Matchers with EitherValues{

  implicit class ItVerbStringExtension(test: ItVerbString) {
    def inIO(op: => IO[Assertion])(implicit pos: source.Position): Unit =
      test.in(op.unsafeRunSync())(pos)
    def inEitherTIO(op: => EitherT[IO, Failure, Assertion])(implicit pos: source.Position): Unit =
      test.in(op.value.unsafeRunSync())(pos)

  }

  implicit class StringVerbStringInvocation(test: ResultOfStringPassedToVerb) {
    def inIO(op: => IO[Assertion])(implicit pos: source.Position): Unit =
      test.in(op.unsafeRunSync())(pos)
    def inEitherTIO(op: => EitherT[IO, Failure, Assertion])(implicit pos: source.Position): Unit =
      test.in(op.value.unsafeRunSync())(pos)
  }

  "getProducts" should "get the list of products from valid json" inEitherTIO {
    val path = getClass.getResource("/validMarket1Product.json").getPath
    for {
      list <- getProducts(path)
    } yield list should not be empty
  }

  it should "return a Left when the file is invalid" inEitherTIO {
    val path = getClass.getResource("/invalidMarket1Product.json").getPath
    for {
      list <- getProducts(path)
    } yield list should be ('left)
  }

}
