package types

import io.circe.{Decoder, Encoder}
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{
  deriveUnwrappedDecoder,
  deriveUnwrappedEncoder
}

@JsonCodec
case class Market(products: List[Product])

@JsonCodec
case class Product(id: ProductId,
                   name: ProductName,
                   price: Price,
                   channelIds: List[ChannelId])
object Product {
  implicit val ordering: Ordering[Product] = (a, b) => {
    val aWorth = a.channelIds.length / a.price.value * 1.0
    val bWorth = b.channelIds.length / b.price.value * 1.0
    //tie-break based on max channels
    if (aWorth == bWorth)
      a.channelIds.length compare b.channelIds.length
    else
      aWorth compare bWorth
  }
}

@JsonCodec(encodeOnly = true)
case class ProductSummary private (id: ProductId,
                                   name: ProductName,
                                   price: Price)
object ProductSummary {
  def apply(p: Product): ProductSummary = ProductSummary(p.id, p.name, p.price)
}

case class ProductId(value: String) extends AnyVal
object ProductId {
  implicit val decoder: Decoder[ProductId] = deriveUnwrappedDecoder
  implicit val encoder: Encoder[ProductId] = deriveUnwrappedEncoder
}

case class ProductName(value: String) extends AnyVal
object ProductName {
  implicit val decoder: Decoder[ProductName] = deriveUnwrappedDecoder
  implicit val encoder: Encoder[ProductName] = deriveUnwrappedEncoder
}

case class Price(value: Int) extends AnyVal
object Price {
  implicit val decoder: Decoder[Price] = deriveUnwrappedDecoder
  implicit val encoder: Encoder[Price] = deriveUnwrappedEncoder
}

case class ChannelId(value: Int) extends AnyVal
object ChannelId {
  implicit val decoder: Decoder[ChannelId] = deriveUnwrappedDecoder
  implicit val encoder: Encoder[ChannelId] = deriveUnwrappedEncoder
}

@JsonCodec(encodeOnly = true)
case class Summary(totalChannels: Int,
                   totalUniqueChannels: Int,
                   totalPrice: Int,
                   uniqueChannelIds: List[ChannelId])

@JsonCodec(encodeOnly = true)
case class Report(summary: Summary, productsToBuy: List[ProductSummary])
object Report {
  def apply(productsToBuy: List[Product]): Report = {
    val totalCh = productsToBuy.length
    val uniqueChannels = productsToBuy.flatMap(_.channelIds).distinct
    val totalPrice = productsToBuy.map(_.price.value).sum

    Report(Summary(totalCh, uniqueChannels.length, totalPrice, uniqueChannels),
           productsToBuy.map(ProductSummary(_)))

  }
}

sealed trait Failure
case class JsonFailure(error: String) extends Failure
case class ArgumentError(error: String) extends Failure