package com.newy.algotrade.coroutine_based_application.price

import com.newy.algotrade.coroutine_based_application.common.web.HttpApiClient
import com.newy.algotrade.domain.common.extension.ProductPrice
import java.time.Duration
import java.time.OffsetDateTime

abstract class ProductPriceHttpApi(protected val client: HttpApiClient) {
    abstract suspend fun productPrices(
        category: String,
        symbol: String,
        interval: Duration,
        endTime: OffsetDateTime,
        limit: Int
    ): List<ProductPrice>
}