package com.newy.algotrade.domain.unit.chart

import com.newy.algotrade.domain.chart.Order
import com.newy.algotrade.domain.chart.OrderHistory
import com.newy.algotrade.domain.chart.OrderType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private fun createOrder(tradeType: OrderType) =
    Order(
        tradeType,
        price = 1000.toDouble().toBigDecimal(),
        quantity = 1.0,
    )

class OrderHistoryTest {
    private lateinit var history: OrderHistory

    @BeforeEach
    fun setUp() {
        history = OrderHistory()
    }

    @Test
    fun `빈 OrderHistory 인 경우`() {
        assertEquals(OrderType.NONE, history.firstOrderType())
        assertEquals(OrderType.NONE, history.lastOrderType())
    }

    @Test
    fun `먼저 Buy Order 를 추가한 경우`() {
        val isAdded = history.add(createOrder(OrderType.BUY))

        assertTrue(isAdded)
        assertEquals(OrderType.BUY, history.firstOrderType())
        assertEquals(OrderType.BUY, history.lastOrderType())
    }

    @Test
    fun `먼저 Sell Order 를 추가한 경우`() {
        val isAdded = history.add(createOrder(OrderType.SELL))

        assertTrue(isAdded)
        assertEquals(OrderType.SELL, history.firstOrderType())
        assertEquals(OrderType.SELL, history.lastOrderType())
    }

    @Test
    fun `같은 OrderType 는 이어서 추가할 수 없다`() {
        val firstAdded = history.add(createOrder(OrderType.BUY))
        val secondAdded = history.add(createOrder(OrderType.BUY))

        assertTrue(firstAdded)
        assertFalse(secondAdded)
    }

    @Test
    fun `다른 OrderType 는 이어서 추가할 수 있다`() {
        val firstAdded = history.add(createOrder(OrderType.BUY))
        val secondAdded = history.add(createOrder(OrderType.SELL))

        assertTrue(firstAdded)
        assertTrue(secondAdded)
        assertEquals(OrderType.BUY, history.firstOrderType())
        assertEquals(OrderType.SELL, history.lastOrderType())
    }
}