package com.newy.algotrade.coroutine_based_application.product.application.service

import com.newy.algotrade.coroutine_based_application.product.port.`in`.SetStrategyUseCase
import com.newy.algotrade.coroutine_based_application.product.port.`in`.model.UserStrategyKey
import com.newy.algotrade.coroutine_based_application.product.port.out.AddStrategyPort
import com.newy.algotrade.coroutine_based_application.product.port.out.GetCandlePort
import com.newy.algotrade.domain.chart.strategy.Strategy

open class SetStrategyService(
    private val candlePort: GetCandlePort,
    private val strategyPort: AddStrategyPort,
) : SetStrategyUseCase {
    override fun setStrategy(key: UserStrategyKey) {
        val candles = candlePort.getCandles(key.productPriceKey)
        val strategy = Strategy.fromClassName(key.strategyClassName, candles)
        strategyPort.addStrategy(key, strategy)
    }
}
