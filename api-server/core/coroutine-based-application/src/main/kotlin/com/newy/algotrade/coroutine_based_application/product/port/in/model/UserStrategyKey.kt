package com.newy.algotrade.coroutine_based_application.product.port.`in`.model

import com.newy.algotrade.domain.price.domain.model.ProductPriceKey

data class UserStrategyKey(
    val userStrategyId: String,
    val strategyClassName: String,
    val productPriceKey: ProductPriceKey,
)