package com.newy.algotrade.coroutine_based_application.price.domain

import com.newy.algotrade.coroutine_based_application.price.domain.model.ProductPriceKey
import com.newy.algotrade.coroutine_based_application.price.port.out.LoadProductPricePort
import com.newy.algotrade.coroutine_based_application.price.port.out.model.LoadProductPriceParam
import com.newy.algotrade.domain.common.extension.ProductPrice
import java.time.OffsetDateTime

class ProductPriceProvider(
    private val loader: LoadProductPricePort
) {
    private val listeners = mutableMapOf<Key, Listener>()
    suspend fun loadInitData() {
        listeners
            .toList()
            .groupByTo(
                mutableMapOf(),
                { (key, _) -> key.productPriceKey },
                { (_, listener) -> listener },
            ).forEach { (productPriceKey, listeners) ->
                loader.productPrices(
                    LoadProductPriceParam(
                        productPriceKey.market,
                        productPriceKey.productType,
                        productPriceKey.productCode,
                        productPriceKey.interval,
                        OffsetDateTime.now(),
                        1,
                    )
                ).let { productPrices ->
                    listeners.forEach { it.onLoadInitData(productPrices) }
                }
            }
    }

    fun putListener(key: Key, listener: Listener) {
        listeners[key] = listener
    }

    fun removeListener(key: Key) {
        listeners.remove(key)
    }

    suspend fun updatePrice(productPriceKey: ProductPriceKey, price: ProductPrice) {
        if (productPriceKey.interval != price.time.period) {
            throw IllegalArgumentException("잘못된 파라미터 입니다.")
        }
        listeners
            .filter { it.key.productPriceKey == productPriceKey }
            .forEach { it.value.onUpdatePrice(productPriceKey, price) }
    }

    data class Key(
        val userStrategyId: String,
        val productPriceKey: ProductPriceKey,
    )

    interface Listener {
        suspend fun onLoadInitData(prices: List<ProductPrice>)
        suspend fun onUpdatePrice(key: ProductPriceKey, price: ProductPrice)
    }
}
