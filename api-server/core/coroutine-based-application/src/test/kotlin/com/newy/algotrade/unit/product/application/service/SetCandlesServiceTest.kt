package com.newy.algotrade.unit.product.application.service

import com.newy.algotrade.coroutine_based_application.product.adapter.out.persistent.InMemoryCandleStore
import com.newy.algotrade.coroutine_based_application.product.application.service.SetCandlesService
import com.newy.algotrade.coroutine_based_application.product.port.`in`.SetCandlesUseCase
import com.newy.algotrade.coroutine_based_application.product.port.out.GetProductPricePort
import com.newy.algotrade.coroutine_based_application.product.port.out.SubscribePollingProductPricePort
import com.newy.algotrade.coroutine_based_application.product.port.out.model.GetProductPriceParam
import com.newy.algotrade.domain.common.extension.ProductPrice
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey
import helpers.productPrice
import helpers.productPriceKey
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals

class SetCandlesServiceTest : GetProductPricePort, SubscribePollingProductPricePort {
    private var apiCallCount = 0
    private var pollingSubscribeCount = 0
    private lateinit var service: SetCandlesUseCase

    override suspend fun getProductPrices(param: GetProductPriceParam): List<ProductPrice> {
        apiCallCount++
        return listOf(
            productPrice(1000, param.productPriceKey.interval)
        )
    }

    override suspend fun subscribe(key: ProductPriceKey) {
        pollingSubscribeCount++
    }

    @BeforeEach
    fun setUp() {
        apiCallCount = 0
        pollingSubscribeCount = 0
        service = SetCandlesService(
            productPricePort = this,
            pollingProductPricePort = this,
            candlePort = InMemoryCandleStore(),
        )
    }

    @Test
    fun `1개 상품만 등록한 경우`() = runBlocking {
        service.setCandles(productPriceKey("BTCUSDT", Duration.ofMinutes(1)))

        assertEquals(1, apiCallCount)
        assertEquals(1, pollingSubscribeCount)
    }

    @Test
    fun `같은 상품을 등록한 경우`() = runBlocking {
        service.setCandles(productPriceKey("BTCUSDT", Duration.ofMinutes(1)))
        service.setCandles(productPriceKey("BTCUSDT", Duration.ofMinutes(1)))

        assertEquals(1, apiCallCount)
        assertEquals(2, pollingSubscribeCount, "폴링은 여러번 요청해도 영향 없음")
    }

    @Test
    fun `다른 상품을 등록한 경우`() = runBlocking {
        service.setCandles(productPriceKey("BTCUSDT", Duration.ofMinutes(1)))
        service.setCandles(productPriceKey("BTCUSDT", Duration.ofMinutes(5)))

        assertEquals(2, apiCallCount)
        assertEquals(2, pollingSubscribeCount)
    }
}