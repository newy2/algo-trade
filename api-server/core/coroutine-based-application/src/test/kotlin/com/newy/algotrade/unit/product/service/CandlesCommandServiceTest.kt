package com.newy.algotrade.unit.product.service

import com.newy.algotrade.coroutine_based_application.product.adapter.out.volatile_storage.InMemoryCandleStoreAdapter
import com.newy.algotrade.coroutine_based_application.product.port.`in`.CandlesUseCase
import com.newy.algotrade.coroutine_based_application.product.port.`in`.ProductPriceQuery
import com.newy.algotrade.coroutine_based_application.product.port.out.CandlePort
import com.newy.algotrade.coroutine_based_application.product.service.CandlesCommandService
import com.newy.algotrade.domain.chart.Candles
import com.newy.algotrade.domain.common.extension.ProductPrice
import com.newy.algotrade.domain.product.ProductPriceKey
import helpers.productPrice
import helpers.productPriceKey
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.test.assertEquals

@DisplayName("setCandles - 외부 API 호출 횟수 확인 테스트")
class SetCandlesServiceApiCallTest : DefaultProductPriceQuery() {
    private val productPriceKey = productPriceKey(productCode = "BTCUSDT", interval = Duration.ofMinutes(1))
    private var apiCallCount = 0
    private var pollingSubscribeCallCount = 0
    private lateinit var service: CandlesUseCase

    override suspend fun getInitProductPrices(productPriceKey: ProductPriceKey): List<ProductPrice> =
        super.getInitProductPrices(productPriceKey).also {
            apiCallCount++
        }

    override fun requestPollingProductPrice(productPriceKey: ProductPriceKey) {
        pollingSubscribeCallCount++
    }

    @BeforeEach
    fun setUp() {
        apiCallCount = 0
        pollingSubscribeCallCount = 0
        service = CandlesCommandService(
            productPriceQuery = this,
            candlePort = InMemoryCandleStoreAdapter(),
        )
    }

    @BeforeEach
    fun `초기 데이터 입력`(): Unit = runBlocking {
        service.setCandles(productPriceKey)
    }

    @Test
    fun `외부 API 호출 횟수 확인`() = runBlocking {
        assertEquals(1, apiCallCount)
        assertEquals(1, pollingSubscribeCallCount)
    }

    @Test
    fun `같은 상품을 등록한 경우`() = runBlocking {
        val sameProductPriceKey = productPriceKey.copy()
        service.setCandles(sameProductPriceKey)

        assertEquals(1, apiCallCount, "ProductPriceKey 가 같으므로 초기 데이터 조회 API 호출하지 않음")
        assertEquals(2, pollingSubscribeCallCount, "폴링은 여러번 요청해도 영향 없음")
    }

    @Test
    fun `다른 상품을 등록한 경우`() = runBlocking {
        val differentProductPriceKey = productPriceKey.copy(
            interval = Duration.ofMinutes(5)
        )
        service.setCandles(differentProductPriceKey)

        assertEquals(2, apiCallCount, "ProductPriceKey 가 다르므로 초기 데이터 조회 API 호출")
        assertEquals(2, pollingSubscribeCallCount)
    }
}

@DisplayName("setCandles - CandlePort 확인 테스트")
class SetCandleCandlePortTest : DefaultProductPriceQuery() {
    private val productPriceKey = productPriceKey(productCode = "BTCUSDT", interval = Duration.ofMinutes(1))
    private lateinit var candlePort: CandlePort
    private lateinit var service: CandlesUseCase

    override suspend fun getInitProductPrices(productPriceKey: ProductPriceKey): List<ProductPrice> {
        return listOf(productPrice(amount = 2000, interval = productPriceKey.interval))
    }

    @BeforeEach
    fun setUp() {
        candlePort = InMemoryCandleStoreAdapter()
        service = CandlesCommandService(
            productPriceQuery = this,
            candlePort = candlePort,
        )
    }

    @BeforeEach
    fun `초기 데이터 입력`(): Unit = runBlocking {
        candlePort.setCandles(
            key = productPriceKey,
            list = listOf(productPrice(amount = 1000, interval = productPriceKey.interval))
        )
    }

    @Test
    fun `CandlePort 저장 상태 확인`() = runBlocking {
        assertCandles(
            expectedFirstCandle = productPrice(amount = 1000, interval = Duration.ofMinutes(1)),
            candles = candlePort.getCandles(productPriceKey)
        )
    }

    @Test
    fun `같은 상품을 등록하는 경우 - candlePort 에 데이터가 저장되어 있으면, 외부 API(fetchInitProductPrices)를 호출하지 않는다`() = runBlocking {
        val sameProductPriceKey = productPriceKey.also {
            service.setCandles(it)
        }

        assertCandles(
            expectedFirstCandle = productPrice(amount = 1000, interval = Duration.ofMinutes(1)),
            candles = candlePort.getCandles(sameProductPriceKey)
        )
    }

    @Test
    fun `다른 상품을 등록한 경우 - 외부 API(fetchInitProductPrices)를 호출해서 candlePort 에 저장한다`() = runBlocking {
        val differentProductPriceKey = productPriceKey.copy(interval = Duration.ofMinutes(5)).also {
            service.setCandles(it)
        }

        assertCandles(
            expectedFirstCandle = productPrice(amount = 1000, interval = Duration.ofMinutes(1)),
            candles = candlePort.getCandles(productPriceKey)
        )
        assertCandles(
            // fetchInitProductPrices 메소드의 결과값으로 등록됨
            expectedFirstCandle = productPrice(amount = 2000, interval = Duration.ofMinutes(5)),
            candles = candlePort.getCandles(differentProductPriceKey)
        )
    }

    private fun assertCandles(expectedFirstCandle: ProductPrice, candles: Candles) {
        assertEquals(1, candles.size)
        assertEquals(expectedFirstCandle, candles.firstCandle)
    }
}

@DisplayName("addCandle - 캔들 추가 테스트")
class AddCandleServiceTest {
    private val productPriceKey = productPriceKey(productCode = "BTCUSDT", interval = Duration.ofMinutes(1))
    private lateinit var candlePort: CandlePort
    private lateinit var service: CandlesCommandService

    @BeforeEach
    fun setUp() {
        candlePort = InMemoryCandleStoreAdapter()
        service = CandlesCommandService(
            productPriceQuery = DefaultProductPriceQuery(),
            candlePort = candlePort,
        )
    }

    @Test
    fun `빈 candles 에 addCandles 하는 경우 - 업데이트가 무시된다`() = runTest {
        service.addCandles(productPriceKey, listOf(productPrice(1000, Duration.ofMinutes(1))))

        assertEquals(0, candlePort.getCandles(productPriceKey).size)
    }

    @Test
    fun `addCandles 를 호출하면 캔들이 추가된다`() = runTest {
        val productPriceList = OffsetDateTime.parse("2024-05-01T00:00Z")
            .let { beginTime ->
                listOf(
                    productPrice(500, Duration.ofMinutes(1), beginTime),
                    productPrice(1000, Duration.ofMinutes(1), beginTime.plusMinutes(1)),
                )
            }.also {
                // 초기 데이터 설정
                candlePort.setCandles(productPriceKey, listOf(it[0]))
            }

        // 이후 데이터 추가
        service.addCandles(productPriceKey, listOf(productPriceList[1]))

        candlePort.getCandles(productPriceKey).let {
            assertEquals(2, it.size)
            assertEquals(productPriceList[0], it.firstCandle)
            assertEquals(productPriceList[1], it.lastCandle)
        }
    }
}

@DisplayName("removeCandles - 캔들 삭제 테스트")
class RemoveCandleServiceTest : DefaultProductPriceQuery() {
    private val productPriceKey = productPriceKey(productCode = "BTCUSDT", interval = Duration.ofMinutes(1))
    private var unPollingSubscribeCallCount = 0
    private lateinit var candlePort: CandlePort
    private lateinit var service: CandlesCommandService

    override fun requestUnPollingProductPrice(productPriceKey: ProductPriceKey) {
        unPollingSubscribeCallCount++
    }

    @BeforeEach
    fun setUp() {
        unPollingSubscribeCallCount = 0
        candlePort = InMemoryCandleStoreAdapter()
        service = CandlesCommandService(
            productPriceQuery = this,
            candlePort = candlePort,
        )
    }

    @Test
    fun `캔들 삭제하기`() {
        candlePort.setCandles(productPriceKey, listOf(productPrice(1000, Duration.ofMinutes(1))))
        assertEquals(1, candlePort.getCandles(productPriceKey).size)
        assertEquals(0, unPollingSubscribeCallCount)

        service.removeCandles(productPriceKey)
        assertEquals(0, candlePort.getCandles(productPriceKey).size)
        assertEquals(1, unPollingSubscribeCallCount)
    }
}

open class DefaultProductPriceQuery : ProductPriceQuery {
    override suspend fun getInitProductPrices(productPriceKey: ProductPriceKey): List<ProductPrice> {
        return listOf(productPrice(1000, productPriceKey.interval))
    }

    override fun requestPollingProductPrice(productPriceKey: ProductPriceKey) {
    }

    override fun requestUnPollingProductPrice(productPriceKey: ProductPriceKey) {
    }
}