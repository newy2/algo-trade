package com.newy.algotrade.integration.product.adapter.out.external_system

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.newy.algotrade.coroutine_based_application.common.web.default_implement.DefaultHttpApiClient
import com.newy.algotrade.coroutine_based_application.product.adapter.out.external_system.FetchByBitProductPrice
import com.newy.algotrade.coroutine_based_application.product.adapter.out.external_system.FetchProductPriceProxy
import com.newy.algotrade.domain.chart.Candle
import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.common.consts.ProductType
import com.newy.algotrade.domain.common.mapper.JsonConverterByJackson
import com.newy.algotrade.domain.common.mapper.toObject
import com.newy.algotrade.domain.product.GetProductPriceHttpParam
import com.newy.algotrade.domain.product.adapter.out.web.model.jackson.ByBitProductPriceHttpResponse
import helpers.TestEnv
import helpers.productPriceKey
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.*
import kotlin.test.assertEquals

@DisplayName("상품 가격조회 API Response DTO")
class ByBitProductPriceResponseDtoTest {
    private val converter = JsonConverterByJackson(jacksonObjectMapper())
    private val extraValues = ByBitProductPriceHttpResponse.jsonExtraValues(interval = 1)

    @Test
    fun `가격 정보가 없는 경우`() {
        val rawResponse = """
            {
                "retCode": 0,
                "retMsg": "OK",
                "result": {
                    "category": "spot",
                    "symbol": "BTCUSDT",
                    "list": []
                },
                "retExtInfo": {},
                "time": 1714575247899
            }
        """.trimIndent()

        val response = converter.toObject<ByBitProductPriceHttpResponse>(rawResponse, extraValues)

        assertEquals(emptyList(), response.prices)
    }

    @Test
    fun `가격 정보가 1개 있는 경우`() {
        val rawResponse = """
            {
                "retCode": 0,
                "retMsg": "OK",
                "result": {
                    "category": "spot",
                    "symbol": "BTCUSDT",
                    "list": [
                        [
                            "1714575660000",
                            "58189.2",
                            "58495.08",
                            "58147.96",
                            "58266.31",
                            "311.07439",
                            "18120659.92086211"
                        ]
                    ]
                },
                "retExtInfo": {},
                "time": 1714575247899
            }
        """.trimIndent()

        val response = converter.toObject<ByBitProductPriceHttpResponse>(rawResponse, extraValues)

        assertEquals(
            listOf(
                Candle.TimeFrame.M1(
                    beginTime = Instant.ofEpochMilli("1714575660000".toLong()).atOffset(ZoneOffset.UTC),
                    openPrice = "58189.2".toBigDecimal(),
                    highPrice = "58495.08".toBigDecimal(),
                    lowPrice = "58147.96".toBigDecimal(),
                    closePrice = "58266.31".toBigDecimal(),
                    volume = "311.07439".toBigDecimal()
                )
            ), response.prices
        )
    }
}

class FetchByBitProductPriceTest {
    private val client = FetchProductPriceProxy(
        mapOf(
            Market.BY_BIT to FetchByBitProductPrice(
                DefaultHttpApiClient(
                    OkHttpClient(),
                    TestEnv.ByBit.url,
                    JsonConverterByJackson(jacksonObjectMapper())
                )
            )
        )
    )

    @Test
    fun `현물 BTC 가격 조회 API`() = runBlocking {
        assertEquals(
            listOf(
                Candle.TimeFrame.M1(
                    beginTime = OffsetDateTime.parse("2024-05-01T00:00Z"),
                    openPrice = "59713.76".toBigDecimal(),
                    highPrice = "59771.9".toBigDecimal(),
                    lowPrice = "59539.96".toBigDecimal(),
                    closePrice = "59747.28".toBigDecimal(),
                    volume = "28.069189".toBigDecimal(),
                )
            ),
            client.getProductPrices(
                GetProductPriceHttpParam(
                    productPriceKey = productPriceKey(
                        productCode = "BTCUSDT",
                        interval = Duration.ofMinutes(1),
                    ),
                    endTime = OffsetDateTime.parse("2024-05-01T00:00Z"),
                    limit = 1,
                )
            )
        )
    }

    @Test
    fun `1일봉 - 현물 BTC 가격 조회 API`() = runBlocking {
        assertEquals(
            listOf(
                Candle.TimeFrame.D1(
                    beginTime = OffsetDateTime.parse("2024-05-01T00:00Z"),
                    openPrice = "59713.76".toBigDecimal(),
                    highPrice = "75100.0".toBigDecimal(),
                    lowPrice = "52241.02".toBigDecimal(),
                    closePrice = "58034.47".toBigDecimal(),
                    volume = "291706.443849".toBigDecimal(),
                )
            ),
            client.getProductPrices(
                GetProductPriceHttpParam(
                    productPriceKey = productPriceKey(
                        productCode = "BTCUSDT",
                        interval = Duration.ofDays(1),
                    ),
                    endTime = OffsetDateTime.parse("2024-05-01T00:00Z"),
                    limit = 1,
                )
            )
        )
    }

    @Test
    fun `무기한 선물 BTC 가격 조회 API`() = runBlocking {
        assertEquals(
            listOf(
                Candle.TimeFrame.M1(
                    beginTime = OffsetDateTime.parse("2024-05-01T00:00Z"),
                    openPrice = "60686.4".toBigDecimal(),
                    highPrice = "60758.9".toBigDecimal(),
                    lowPrice = "60673.4".toBigDecimal(),
                    closePrice = "60737.2".toBigDecimal(),
                    volume = "0.632".toBigDecimal(),
                )
            ),
            client.getProductPrices(
                GetProductPriceHttpParam(
                    productPriceKey = productPriceKey(
                        productCode = "BTCUSDT",
                        productType = ProductType.PERPETUAL_FUTURE,
                        interval = Duration.ofMinutes(1),
                    ),
                    endTime = OffsetDateTime.parse("2024-05-01T00:00Z"),
                    limit = 1,
                )
            )
        )
    }
}