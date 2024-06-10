package com.newy.algotrade.coroutine_based_application.product.port.`in`.model

import com.newy.algotrade.domain.price.domain.model.ProductPriceKey
import java.time.OffsetDateTime

data class BackTestingDataKey(
    val productPriceKey: ProductPriceKey,
    val searchBeginTime: OffsetDateTime,
    val searchEndTime: OffsetDateTime,
)