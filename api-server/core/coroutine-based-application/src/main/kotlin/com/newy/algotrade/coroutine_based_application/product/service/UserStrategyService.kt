package com.newy.algotrade.coroutine_based_application.product.service

import com.newy.algotrade.coroutine_based_application.product.port.`in`.UserStrategyQuery
import com.newy.algotrade.coroutine_based_application.product.port.`in`.model.UserStrategyKey
import com.newy.algotrade.coroutine_based_application.product.port.out.UserStrategyQueryPort

open class UserStrategyService(
    private val userStrategyPort: UserStrategyQueryPort
) : UserStrategyQuery {
    override suspend fun getAllUserStrategies(): List<UserStrategyKey> {
        return userStrategyPort.getAllUserStrategies()
    }

    override suspend fun getUserStrategy(userStrategyId: Long): UserStrategyKey? {
        return userStrategyPort.getUserStrategy(userStrategyId)
    }
}