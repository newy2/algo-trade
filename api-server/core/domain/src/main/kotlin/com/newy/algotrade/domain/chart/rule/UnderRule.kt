package com.newy.algotrade.domain.chart.rule

import com.newy.algotrade.domain.chart.Rule
import com.newy.algotrade.domain.chart.indicator.Indicator
import com.newy.algotrade.domain.chart.order.OrderSignalHistory

class UnderRule(private val first: Indicator, private val second: Indicator) : Rule {
    override fun isSatisfied(index: Int, history: OrderSignalHistory?): Boolean {
        return first[index] < second[index]
    }

}
