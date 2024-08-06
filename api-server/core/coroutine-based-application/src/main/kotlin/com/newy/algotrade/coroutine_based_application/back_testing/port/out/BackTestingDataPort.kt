package com.newy.algotrade.coroutine_based_application.back_testing.port.out

import com.newy.algotrade.domain.back_testing.BackTestingDataKey
import com.newy.algotrade.domain.common.extension.ProductPrice

interface BackTestingDataPort : GetBackTestingDataPort, SetBackTestingDataPort

interface SetBackTestingDataPort {
    suspend fun setBackTestingData(key: BackTestingDataKey, list: List<ProductPrice>)
}

interface GetBackTestingDataPort {
    suspend fun getBackTestingData(key: BackTestingDataKey): List<ProductPrice>
}