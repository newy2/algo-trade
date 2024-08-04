package com.newy.algotrade.coroutine_based_application.user_strategy.port.out

fun interface GetMarketPort {
    suspend fun getMarketIdsBy(marketAccountId: Long): List<Long>
}
