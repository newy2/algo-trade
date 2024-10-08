package com.newy.algotrade.domain.product_price.jackson

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.newy.algotrade.domain.chart.Candle
import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.common.consts.ProductType
import com.newy.algotrade.domain.common.extension.ProductPrice
import com.newy.algotrade.domain.product_price.ProductPriceKey
import java.time.Duration

@JsonIgnoreProperties(ignoreUnknown = true)
class ByBitProductPriceWebSocketResponse(
    val productPriceKey: ProductPriceKey,
    val prices: List<ProductPrice>,
) {
    companion object {
        fun jsonExtraValues(productType: ProductType) =
            mapOf("productType" to productType.name)
    }

    @JsonCreator
    constructor(
        @JacksonInject("productType") productType: String,
        @JsonProperty("topic") topic: String,
        @JsonProperty("data") node: JsonNode,
    ) : this(
        productPriceKey = topic.split(".").let { (_, duration, productCode) ->
            ProductPriceKey(
                Market.BY_BIT,
                ProductType.valueOf(productType),
                productCode,
                if (duration == "D") Duration.ofDays(1) else Duration.ofMinutes(duration.toLong())
            )
        },
        prices = node.map {
            Candle.TimeFrame.from(Duration.ofMinutes(it["interval"].asLong()))!!(
                beginTime = it["start"].asLong(),
                openPrice = it["open"].asDouble().toBigDecimal(),
                highPrice = it["high"].asDouble().toBigDecimal(),
                lowPrice = it["low"].asDouble().toBigDecimal(),
                closePrice = it["close"].asDouble().toBigDecimal(),
                volume = it["volume"].asDouble().toBigDecimal()
            )
        }
    )
}