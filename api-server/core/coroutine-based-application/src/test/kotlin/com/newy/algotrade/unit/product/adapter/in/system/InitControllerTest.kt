package com.newy.algotrade.unit.product.adapter.`in`.system

import com.newy.algotrade.coroutine_based_application.product.adapter.`in`.system.InitController
import com.newy.algotrade.coroutine_based_application.product.port.`in`.GetAllUserStrategyQuery
import com.newy.algotrade.coroutine_based_application.product.port.`in`.SetCandlesUseCase
import com.newy.algotrade.coroutine_based_application.run_strategy.port.`in`.StrategyUseCase
import com.newy.algotrade.coroutine_based_application.run_strategy.port.`in`.model.UserStrategyKey
import com.newy.algotrade.coroutine_based_application.run_strategy.service.RunnableStrategyCommandService
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey
import helpers.productPriceKey
import helpers.userStrategyKey
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class InitControllerTest : GetAllUserStrategyQuery, SetCandlesUseCase, NoErrorStrategyUseCase {
    private var log: String = ""
    override suspend fun getAllUserStrategies(): List<UserStrategyKey> {
        log += "getAllUserStrategies "
        return listOf(
            userStrategyKey("id1", productPriceKey("BTCUSDT", Duration.ofMinutes(1))),
            userStrategyKey("id2", productPriceKey("BTCUSDT", Duration.ofMinutes(1))),
        )
    }

    override suspend fun setCandles(productPriceKey: ProductPriceKey) {
        log += "setCandles "
    }

    override fun setStrategy(key: UserStrategyKey) {
        log += "setStrategy "
    }

    @Test
    fun `UseCase 호출 순서 확인`() = runTest {
        val controller = InitController(
            RunnableStrategyCommandService(
                candlesUseCase = this@InitControllerTest,
                strategyUseCase = this@InitControllerTest
            ),
            userStrategyQuery = this@InitControllerTest,
        )

        controller.init()

        Assertions.assertEquals("getAllUserStrategies setCandles setStrategy setCandles setStrategy ", log)
    }
}

interface NoErrorStrategyUseCase : StrategyUseCase {
    override fun removeStrategy(key: UserStrategyKey) {
        TODO("Not yet implemented")
    }
}