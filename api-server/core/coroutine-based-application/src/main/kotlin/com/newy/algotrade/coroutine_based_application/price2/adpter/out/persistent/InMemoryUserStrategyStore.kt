package com.newy.algotrade.coroutine_based_application.price2.adpter.out.persistent

import com.newy.algotrade.coroutine_based_application.price2.port.`in`.model.UserStrategyKey
import com.newy.algotrade.coroutine_based_application.price2.port.out.UserStrategyPort
import com.newy.algotrade.domain.chart.strategy.Strategy
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey

class InMemoryUserStrategyStore : UserStrategyPort {
    private val map = mutableMapOf<UserStrategyKey, Strategy>()
    override fun hasProductPriceKey(productPriceKey: ProductPriceKey): Boolean {
        return map.keys.find { it.productPriceKey == productPriceKey } != null
    }

    override fun add(key: UserStrategyKey, strategy: Strategy) {
        map[key] = strategy
    }

    override fun remove(key: UserStrategyKey) {
        map.remove(key)
    }

    override fun getStrategyList(productPriceKey: ProductPriceKey): List<Strategy> {
        return map
            .filter { it.key.productPriceKey == productPriceKey }
            .values
            .toList()
    }
}