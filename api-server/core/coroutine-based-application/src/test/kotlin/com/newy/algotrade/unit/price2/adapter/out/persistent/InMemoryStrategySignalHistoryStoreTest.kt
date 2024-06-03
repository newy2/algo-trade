package com.newy.algotrade.unit.price2.adapter.out.persistent

import com.newy.algotrade.coroutine_based_application.price2.adapter.out.persistent.InMemoryStrategySignalHistoryStore
import com.newy.algotrade.coroutine_based_application.price2.port.out.StrategySignalHistoryPort
import com.newy.algotrade.domain.chart.Candle
import com.newy.algotrade.domain.chart.order.OrderType
import com.newy.algotrade.domain.chart.strategy.StrategySignal
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.test.assertEquals

class InMemoryStrategySignalHistoryStoreTest {
    private val signal = StrategySignal(
        OrderType.BUY,
        Candle.TimeRange(
            Duration.ofMinutes(1),
            OffsetDateTime.parse("2024-05-09T00:00+09:00")
        ),
        1000.toBigDecimal()
    )

    private lateinit var store: StrategySignalHistoryPort

    @BeforeEach
    fun setUp() {
        store = InMemoryStrategySignalHistoryStore().also {
            it.addHistory("id1", signal)
        }
    }

    @Test
    fun `등록한 히스토리 가져오기`() {
        val registeredId = "id1"
        val history = store.getHistory(registeredId)

        history.orders().let {
            assertEquals(1, it.size)
            assertEquals(signal, it.first())
        }
    }

    @Test
    fun `등록하지 않은 히스토리 가져오기`() {
        val unRegisteredId = "id2"
        val history = store.getHistory(unRegisteredId)

        assertTrue(history.isEmpty())
    }

    @Test
    fun `등록한 히스토리 삭제하기`() {
        store.removeHistory("id1")

        val removedId = "id1"
        val history = store.getHistory(removedId)

        assertTrue(history.isEmpty())
    }
}