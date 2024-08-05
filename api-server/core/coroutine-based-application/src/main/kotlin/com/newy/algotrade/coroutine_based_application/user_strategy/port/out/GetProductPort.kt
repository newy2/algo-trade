package com.newy.algotrade.coroutine_based_application.user_strategy.port.out

import com.newy.algotrade.domain.common.consts.ProductType
import com.newy.algotrade.domain.user_strategy.Product

fun interface GetProductPort {
    suspend fun getProducts(
        marketIds: List<Long>,
        productType: ProductType,
        productCodes: List<String>
    ): List<Product>
}