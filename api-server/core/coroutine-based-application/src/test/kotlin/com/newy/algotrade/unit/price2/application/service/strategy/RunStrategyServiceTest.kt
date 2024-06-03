package com.newy.algotrade.unit.price2.application.service.strategy

import com.newy.algotrade.coroutine_based_application.price2.adapter.out.persistent.InMemoryCandleStore
import com.newy.algotrade.coroutine_based_application.price2.adapter.out.persistent.InMemoryStrategySignalHistoryStore
import com.newy.algotrade.coroutine_based_application.price2.adapter.out.persistent.InMemoryStrategyStore
import com.newy.algotrade.coroutine_based_application.price2.application.service.strategy.RunStrategyService
import com.newy.algotrade.coroutine_based_application.price2.port.`in`.strategy.RunStrategyUseCase
import com.newy.algotrade.coroutine_based_application.price2.port.out.*
import com.newy.algotrade.domain.chart.order.OrderSignal
import com.newy.algotrade.domain.chart.order.OrderType
import com.newy.algotrade.domain.chart.strategy.Strategy
import helpers.BooleanRule
import helpers.productPrice
import helpers.productPriceKey
import helpers.userStrategyKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.OffsetDateTime

private val now = OffsetDateTime.parse("2024-05-01T00:00Z")

class BooleanStrategy(entry: Boolean, exit: Boolean) : Strategy(
    OrderType.BUY,
    BooleanRule(entry),
    BooleanRule(exit),
) {
    override fun version() = "0"
}

private val BTC_1MINUTE = productPriceKey("BTCUSDT", Duration.ofMinutes(1))
private val ETH_1MINUTE = productPriceKey("ETHUSDT", Duration.ofMinutes(1))

@DisplayName("전략 실행하기 테스트")
class RunStrategyServiceTest : OnCreateStrategySignalPort {
    private lateinit var service: RunStrategyUseCase
    private lateinit var results: MutableMap<String, OrderSignal>

    override fun onCreateSignal(userStrategyId: String, orderSignal: OrderSignal) {
        results[userStrategyId] = orderSignal
    }

    @BeforeEach
    fun setUp() {
        service = RunStrategyService(
            candlePort = InMemoryCandleStore().also {
                it.setCandles(
                    BTC_1MINUTE, listOf(
                        productPrice(1000, Duration.ofMinutes(1), now.plusMinutes(0)),
                        productPrice(2000, Duration.ofMinutes(1), now.plusMinutes(1)),
                    )
                )
                it.setCandles(
                    ETH_1MINUTE, listOf(
                        productPrice(1000, Duration.ofMinutes(1), now.plusMinutes(0)),
                    )
                )
            },
            strategyPort = InMemoryStrategyStore().also {
                it.add(userStrategyKey("id1", BTC_1MINUTE), BooleanStrategy(entry = true, exit = true))
                it.add(userStrategyKey("id2", BTC_1MINUTE), BooleanStrategy(entry = false, exit = false))
                it.add(userStrategyKey("id3", ETH_1MINUTE), BooleanStrategy(entry = true, exit = false))
            },
            strategySignalHistoryPort = InMemoryStrategySignalHistoryStore(),
            strategySignalPort = this
        )
        results = mutableMapOf()
    }

    @Test
    fun `BTC 상품코드로 실행`() {
        service.runStrategy(BTC_1MINUTE)

        val lastPrice = productPrice(2000, Duration.ofMinutes(1), now.plusMinutes(1))
        val expected = mapOf("id1" to OrderSignal(OrderType.BUY, lastPrice.time, lastPrice.price.close))

        assertEquals(expected, results, "OrderSignal 은 Candles#lastCandle 값으로 생성되야 한다")
    }

    @Test
    fun `ETH 상품 코드로 실행`() {
        service.runStrategy(ETH_1MINUTE)

        val lastPrice = productPrice(1000, Duration.ofMinutes(1), now.plusMinutes(0))
        val expected = mapOf("id3" to OrderSignal(OrderType.BUY, lastPrice.time, lastPrice.price.close))

        assertEquals(expected, results, "OrderSignal 은 Candles#lastCandle 값으로 생성되야 한다")
    }
}