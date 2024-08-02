package com.newy.algotrade.coroutine_based_application.product.port.out

import com.newy.algotrade.domain.chart.Candles
import com.newy.algotrade.domain.common.extension.ProductPrice
import com.newy.algotrade.domain.price.ProductPriceKey

interface CandlePort : CandleQueryPort, CandleCommandPort

interface CandleQueryPort {
    fun getCandles(key: ProductPriceKey): Candles
    fun hasCandles(key: ProductPriceKey): Boolean
}

interface CandleCommandPort {
    fun addCandles(key: ProductPriceKey, list: List<ProductPrice>): Candles
    fun removeCandles(key: ProductPriceKey)
    fun setCandles(key: ProductPriceKey, list: List<ProductPrice>): Candles
}