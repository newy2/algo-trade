package com.newy.algotrade.coroutine_based_application.price2.port.out

import com.newy.algotrade.coroutine_based_application.price2.port.`in`.model.BackTestingDataKey
import com.newy.algotrade.domain.common.extension.ProductPrice

interface BackTestingDataPort : GetBackTestingDataPort, SetBackTestingDataPort

interface SetBackTestingDataPort {
    suspend fun setBackTestingData(key: BackTestingDataKey, list: List<ProductPrice>)
}

interface GetBackTestingDataPort {
    suspend fun getBackTestingData(key: BackTestingDataKey): List<ProductPrice>
}