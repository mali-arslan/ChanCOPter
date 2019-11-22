import scala.io.Source
object Main extends App {
  val marketJson = Source.fromFile("resources/market.json").getLines().mkString("\n")
  println(marketJson)
}
