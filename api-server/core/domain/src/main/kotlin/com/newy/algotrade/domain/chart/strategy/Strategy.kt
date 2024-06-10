package com.newy.algotrade.domain.chart.strategy

import com.newy.algotrade.domain.chart.Candles
import com.newy.algotrade.domain.chart.Rule
import com.newy.algotrade.domain.chart.order.OrderType
import com.newy.algotrade.domain.chart.strategy.custom.BuyTripleRSIStrategy
import com.newy.algotrade.domain.chart.strategy.custom.BuyTripleRSIStrategyV2

enum class StrategyId(val id: String) {
    BuyTripleRSIStrategy("1"),
    BuyTripleRSIStrategyV2("2");

    companion object {
        private val map = StrategyId.values().associateBy { it.id }
        infix fun from(id: String) = map.getValue(id)
    }
}

abstract class Strategy(private val entryType: OrderType, private val entryRule: Rule, private val exitRule: Rule) {
    companion object {
        fun create(id: StrategyId, candles: Candles): Strategy {
            return when (id) {
                StrategyId.BuyTripleRSIStrategy -> BuyTripleRSIStrategy(candles)
                StrategyId.BuyTripleRSIStrategyV2 -> BuyTripleRSIStrategyV2(candles)
            }
        }
    }

    init {
        if (entryType == OrderType.NONE) {
            throw IllegalArgumentException("사용할 수 없는 OrderType 입니다")
        }
    }

    abstract fun version(): String

    fun shouldOperate(index: Int, history: StrategySignalHistory): OrderType {
        validate(history)

        if (shouldEnter(index, history)) {
            return entryType
        }

        if (shouldExit(index, history)) {
            return entryType.completedType()
        }

        return OrderType.NONE
    }

    private fun validate(history: StrategySignalHistory) {
        // TODO 이거 수상하다. 나중에 필요 없으면 삭제해도 될듯.
        if (!arrayOf(OrderType.NONE, entryType).contains(history.firstOrderType())) {
            throw IllegalArgumentException("entryOrderType 이 다릅니다")
        }
    }

    private fun shouldEnter(index: Int, history: StrategySignalHistory): Boolean =
        entryRule.isSatisfied(index, history) && !history.isOpened()

    private fun shouldExit(index: Int, history: StrategySignalHistory): Boolean =
        exitRule.isSatisfied(index, history) && history.isOpened()
}
