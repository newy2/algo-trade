package com.newy.algotrade.unit.back_testing.service

import com.newy.algotrade.coroutine_based_application.back_testing.adapter.out.persistent.FileBackTestingDataStore
import com.newy.algotrade.coroutine_based_application.back_testing.domain.BackTestingFileManager
import com.newy.algotrade.coroutine_based_application.back_testing.port.`in`.model.BackTestingDataKey
import com.newy.algotrade.coroutine_based_application.back_testing.service.SetBackTestingDataService
import com.newy.algotrade.domain.chart.Candle
import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.common.consts.ProductType
import com.newy.algotrade.domain.price.ProductPriceKey
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SetBackTestingDataServiceTest {
    private val key = BackTestingDataKey(
        ProductPriceKey(
            Market.BY_BIT,
            ProductType.SPOT,
            "BTCUSDT",
            Duration.ofMinutes(1)
        ),
        searchBeginTime = OffsetDateTime.parse("8888-01-01T00:00+09:00"),
        searchEndTime = OffsetDateTime.parse("8888-01-01T00:01+09:00"),
    )
    private val fileManager = BackTestingFileManager()
    private val service = SetBackTestingDataService(FileBackTestingDataStore(fileManager))

    @AfterClass
    fun tearDown() {
        Paths.get(fileManager.folderPath(), fileManager.fileName(key)).toFile().deleteOnExit()
    }

    @Test
    fun `빈 리스트 저장하기`() = runTest {
        val isSaved = service.setBackTestingData(key, emptyList())

        assertFalse(isSaved)
    }

    @Test
    fun `리스트 저장하기`() = runTest {
        val list = listOf(
            Candle.TimeFrame.M1(
                OffsetDateTime.parse("8888-01-01T00:00+09:00"),
                openPrice = 200.0.toBigDecimal(),
                highPrice = 400.0.toBigDecimal(),
                lowPrice = 100.0.toBigDecimal(),
                closePrice = 300.0.toBigDecimal(),
                volume = 50.0.toBigDecimal(),
            )
        )

        val isSaved = service.setBackTestingData(key, list)

        assertTrue(isSaved)
    }
}