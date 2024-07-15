package com.newy.algotrade.web_flux.product.adapter.out.persistent

import com.newy.algotrade.coroutine_based_application.product.port.`in`.model.UserStrategyKey
import com.newy.algotrade.coroutine_based_application.product.port.out.GetUserStrategyPort
import com.newy.algotrade.domain.chart.Candle
import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.common.consts.ProductType
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey
import com.newy.algotrade.web_flux.product.adapter.out.persistent.repository.UserStrategyWithProductRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class GetUserStrategyPersistenceAdapter(
    private val repository: UserStrategyWithProductRepository
) : GetUserStrategyPort {
    override suspend fun getAllUserStrategies(): List<UserStrategyKey> =
        repository
            .findAllWithProducts()
            .toList()
            .groupBy { it.id }
            .flatMap { (id, values) ->
                values.map {
                    UserStrategyKey(
                        userStrategyId = id.toString(),
                        strategyClassName = it.strategyClassName,
                        productPriceKey = ProductPriceKey(
                            market = Market.valueOf(it.marketCode),
                            productType = ProductType.valueOf(it.productType),
                            productCode = it.productCode,
                            interval = Candle.TimeFrame.valueOf(it.timeFrame).timePeriod,
                        ),
                    )
                }
            }
}