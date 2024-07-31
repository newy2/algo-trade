package com.newy.algotrade.unit.run_strategy.adapter.`in`.web

import com.newy.algotrade.coroutine_based_application.product.adapter.`in`.web.SetRunnableStrategyController
import com.newy.algotrade.coroutine_based_application.product.port.`in`.SetCandlesUseCase
import com.newy.algotrade.coroutine_based_application.run_strategy.port.`in`.StrategyUseCase
import com.newy.algotrade.coroutine_based_application.run_strategy.port.`in`.model.UserStrategyKey
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey
import helpers.productPriceKey
import helpers.userStrategyKey
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SetRunnableStrategyControllerTest : SetCandlesUseCase, NoErrorStrategyUseCase {
    private var log: String = ""

    override suspend fun setCandles(productPriceKey: ProductPriceKey) {
        log += "setCandles "
    }

    override fun setStrategy(key: UserStrategyKey) {
        log += "setStrategy "
    }

    @Test
    fun `UseCase 호출 순서 확인`() = runTest {
        val controller = SetRunnableStrategyController(
            this@SetRunnableStrategyControllerTest,
            this@SetRunnableStrategyControllerTest
        )

        val key = userStrategyKey("id1", productPriceKey("BTCUSDT"))
        controller.setUserStrategy(key)

        Assertions.assertEquals("setCandles setStrategy ", log)
    }
}

interface NoErrorStrategyUseCase : StrategyUseCase {
    override fun removeStrategy(key: UserStrategyKey) {
        TODO("Not yet implemented")
    }
}