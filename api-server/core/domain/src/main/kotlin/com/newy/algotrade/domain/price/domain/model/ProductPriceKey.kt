package com.newy.algotrade.domain.price.domain.model

import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.common.consts.ProductType
import java.time.Duration

data class ProductPriceKey(
    val market: Market,
    val productType: ProductType,
    val productCode: String,
    val interval: Duration,
)