package com.newy.algotrade.coroutine_based_application.price.domain

import com.newy.algotrade.domain.chart.Candles
import com.newy.algotrade.domain.chart.strategy.Strategy
import com.newy.algotrade.domain.chart.strategy.StrategyRunner
import com.newy.algotrade.domain.chart.strategy.StrategySignalHistory
import com.newy.algotrade.domain.common.extension.ProductPrice
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey

@Deprecated("미사용 코드")
class UserStrategyRunner(
    val productPriceProviderKey: ProductPriceProvider.Key,
    private val candles: Candles,
    strategy: Strategy,
    val history: StrategySignalHistory = StrategySignalHistory(),
    private val strategyRunner: StrategyRunner = StrategyRunner(candles, strategy, history)
) : ProductPriceProvider.Listener {
    override suspend fun onLoadInitData(prices: List<ProductPrice>) {
        candles.upsert(prices)
    }

    override suspend fun onUpdatePrice(key: ProductPriceKey, prices: List<ProductPrice>) {
        // TODO candle#upsert 를 여기서 할까?
        strategyRunner.run(prices)
    }
}