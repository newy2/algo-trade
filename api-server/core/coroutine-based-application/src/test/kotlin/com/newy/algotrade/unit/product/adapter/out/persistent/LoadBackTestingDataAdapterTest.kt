package com.newy.algotrade.unit.product.adapter.out.persistent

import com.newy.algotrade.coroutine_based_application.product.adapter.out.persistent.LoadBackTestingDataAdapter
import com.newy.algotrade.coroutine_based_application.product.port.`in`.model.BackTestingDataKey
import com.newy.algotrade.coroutine_based_application.product.port.out.GetBackTestingDataPort
import com.newy.algotrade.coroutine_based_application.product.port.out.OnReceivePollingPricePort
import com.newy.algotrade.coroutine_based_application.product.port.out.model.GetProductPriceParam
import com.newy.algotrade.domain.common.extension.ProductPrice
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey
import helpers.productPrice
import helpers.productPriceKey
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.OffsetDateTime

class LoadBackTestingDataAdapterTest : GetBackTestingDataPort, OnReceivePollingPricePort {
    private val interval = Duration.ofMinutes(1)
    private val fakeKey = BackTestingDataKey(
        productPriceKey("BTCUSDT", interval),
        OffsetDateTime.parse("2024-05-01T00:00Z"),
        OffsetDateTime.parse("2024-05-01T00:03Z")
    )
    private val loader = LoadBackTestingDataAdapter(
        fakeKey,
        this,
        this
    )
    private val fakeParam = GetProductPriceParam(
        fakeKey.productPriceKey,
        OffsetDateTime.parse("2024-05-01T00:03Z"),
        limit = 2
    )

    private lateinit var receiveDataList: MutableList<ProductPrice>

    override suspend fun getBackTestingData(key: BackTestingDataKey): List<ProductPrice> {
        return listOf(
            productPrice(100, interval, OffsetDateTime.parse("2024-05-01T00:00Z")),
            productPrice(200, interval, OffsetDateTime.parse("2024-05-01T00:01Z")),
            productPrice(300, interval, OffsetDateTime.parse("2024-05-01T00:02Z")),
            productPrice(400, interval, OffsetDateTime.parse("2024-05-01T00:03Z")),
            productPrice(500, interval, OffsetDateTime.parse("2024-05-01T00:04Z")),
        )
    }

    override suspend fun onReceivePrice(productPriceKey: ProductPriceKey, productPriceList: List<ProductPrice>) {
        receiveDataList.add(productPriceList.first())
    }

    @BeforeEach
    fun setUp() {
        receiveDataList = mutableListOf()
    }

    @Test
    fun getProductPrices() = runTest {
        val firstDataList = loader.getProductPrices(fakeParam)

        assertEquals(
            listOf(
                productPrice(100, interval, OffsetDateTime.parse("2024-05-01T00:00Z")),
                productPrice(200, interval, OffsetDateTime.parse("2024-05-01T00:01Z")),
            ),
            firstDataList
        )
    }

    @Test
    fun await() = runTest {
        loader.getProductPrices(fakeParam)

        loader.await()

        assertEquals(
            listOf(
                productPrice(300, interval, OffsetDateTime.parse("2024-05-01T00:02Z")),
                productPrice(400, interval, OffsetDateTime.parse("2024-05-01T00:03Z")),
                productPrice(500, interval, OffsetDateTime.parse("2024-05-01T00:04Z")),
            ),
            receiveDataList
        )
    }
}