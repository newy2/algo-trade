package com.newy.algotrade.coroutine_based_application.product.port.`in`

import com.newy.algotrade.domain.chart.Candles
import com.newy.algotrade.domain.price.ProductPriceKey

interface CandlesQuery {
    fun getCandles(key: ProductPriceKey): Candles
}