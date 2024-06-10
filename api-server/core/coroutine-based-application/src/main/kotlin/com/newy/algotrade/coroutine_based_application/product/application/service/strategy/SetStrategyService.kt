package com.newy.algotrade.coroutine_based_application.product.application.service.strategy

import com.newy.algotrade.coroutine_based_application.product.port.`in`.strategy.SetStrategyUseCase
import com.newy.algotrade.coroutine_based_application.product.port.`in`.strategy.model.UserStrategyKey
import com.newy.algotrade.coroutine_based_application.product.port.out.AddStrategyPort
import com.newy.algotrade.coroutine_based_application.product.port.out.GetCandlePort
import com.newy.algotrade.domain.chart.strategy.Strategy

class SetStrategyService(
    private val candlePort: GetCandlePort,
    private val strategyPort: AddStrategyPort,
) : SetStrategyUseCase {
    override fun setStrategy(key: UserStrategyKey) {
        val candles = candlePort.getCandles(key.productPriceKey)
        val strategy = Strategy.create(key.strategyId, candles)
        strategyPort.addStrategy(key, strategy)
    }
}
