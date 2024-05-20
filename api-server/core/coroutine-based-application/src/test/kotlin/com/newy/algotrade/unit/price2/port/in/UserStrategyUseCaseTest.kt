package com.newy.algotrade.unit.price2.port.`in`

import com.newy.algotrade.coroutine_based_application.price2.adpter.out.persistent.InMemoryCandleStore
import com.newy.algotrade.coroutine_based_application.price2.port.`in`.RegisterUserStrategyUseCase
import com.newy.algotrade.coroutine_based_application.price2.port.`in`.UnRegisterUserStrategyUseCase
import com.newy.algotrade.coroutine_based_application.price2.port.`in`.model.UserStrategyKey
import com.newy.algotrade.coroutine_based_application.price2.port.out.CreateUserStrategyPort
import com.newy.algotrade.coroutine_based_application.price2.port.out.DeleteUserStrategyPort
import com.newy.algotrade.domain.chart.strategy.Strategy
import com.newy.algotrade.domain.chart.strategy.StrategyId
import com.newy.algotrade.domain.chart.strategy.custom.BuyTripleRSIStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertTrue

private val userStrategyKey = UserStrategyKey(
    "user1",
    StrategyId.BuyTripleRSIStrategy,
    productPriceKey("BTCUSDT", Duration.ofMinutes(1))
)

class UserStrategyUseCaseTest : CreateUserStrategyPort, DeleteUserStrategyPort {
    private var addedCount = 0
    private var removedCount = 0
    private lateinit var strategy: Strategy

    override fun add(key: UserStrategyKey, strategy: Strategy) {
        this.strategy = strategy
        addedCount++
    }

    override fun remove(key: UserStrategyKey) {
        removedCount++
    }

    @BeforeEach
    fun setUp() {
        addedCount = 0
        removedCount = 0
    }

    @Test
    fun `등록하기`() {
        val service = RegisterUserStrategyUseCase(InMemoryCandleStore(), this)

        service.register(userStrategyKey)

        assertEquals(1, addedCount)
        assertTrue(strategy is BuyTripleRSIStrategy)
    }

    @Test
    fun `해제하기`() {
        val service = UnRegisterUserStrategyUseCase(this)

        service.unRegister(userStrategyKey)

        assertEquals(1, removedCount)
    }
}