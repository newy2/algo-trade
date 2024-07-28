package com.newy.algotrade.integration.product.adapter.out.persistent

import com.newy.algotrade.coroutine_based_application.product.port.`in`.model.UserStrategyKey
import com.newy.algotrade.domain.chart.Candle
import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.common.consts.ProductCategory
import com.newy.algotrade.domain.common.consts.ProductType
import com.newy.algotrade.domain.price.domain.model.ProductPriceKey
import com.newy.algotrade.web_flux.market_account.adapter.out.persistent.repository.MarketAccountRepository
import com.newy.algotrade.web_flux.product.adapter.out.persistent.GetUserStrategyPersistenceAdapter
import com.newy.algotrade.web_flux.user_strategy.adapter.out.persistent.repository.*
import helpers.BaseDbTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetAllUserStrategiesTest : BaseUserStrategyPersistenceAdapterTest() {
    @Test
    fun `user strategy 가 1개인 경우`() = runTransactional {
        val userStrategyId = setInitData(strategyClassName = "A", listOf("BTCUSDT"))

        val actualList = adapter.getAllUserStrategies()
        val expectedList = listOf(
            UserStrategyKey(
                userStrategyId = userStrategyId.toString(),
                strategyClassName = "A",
                productPriceKey = ProductPriceKey(
                    market = Market.BY_BIT,
                    productType = ProductType.SPOT,
                    productCode = "BTCUSDT",
                    interval = Candle.TimeFrame.M1.timePeriod,
                ),
            ),
        )

        assertEquals(expectedList, actualList)
    }


    @Test
    fun `user strategy 가 2개 이상인 경우`() = runTransactional {
        val userStrategyId1 = setInitData(strategyClassName = "A", listOf("BTCUSDT"))
        val userStrategyId2 = setInitData(strategyClassName = "B", listOf("ETHUSDT", "SOLUSDT"))

        val actualList = adapter.getAllUserStrategies()
        val expectedList = listOf(
            UserStrategyKey(
                userStrategyId = userStrategyId1.toString(),
                strategyClassName = "A",
                productPriceKey = ProductPriceKey(
                    market = Market.BY_BIT,
                    productType = ProductType.SPOT,
                    productCode = "BTCUSDT",
                    interval = Candle.TimeFrame.M1.timePeriod,
                ),
            ),
            UserStrategyKey(
                userStrategyId = userStrategyId2.toString(),
                strategyClassName = "B",
                productPriceKey = ProductPriceKey(
                    market = Market.BY_BIT,
                    productType = ProductType.SPOT,
                    productCode = "ETHUSDT",
                    interval = Candle.TimeFrame.M1.timePeriod,
                ),
            ),
            UserStrategyKey(
                userStrategyId = userStrategyId2.toString(),
                strategyClassName = "B",
                productPriceKey = ProductPriceKey(
                    market = Market.BY_BIT,
                    productType = ProductType.SPOT,
                    productCode = "SOLUSDT",
                    interval = Candle.TimeFrame.M1.timePeriod,
                ),
            )
        )

        assertEquals(expectedList, actualList)
    }
}

class GetUserStrategyTest : BaseUserStrategyPersistenceAdapterTest() {
    @Test
    fun `ID 가 없는 경우`() = runTransactional {
        val unRegisteredId: Long = 100

        assertNull(adapter.getUserStrategy(unRegisteredId))
    }

    @Test
    fun `ID 가 있는 경우`() = runTransactional {
        val userStrategyId = setInitData(strategyClassName = "A", listOf("BTCUSDT"))

        val actual = adapter.getUserStrategy(userStrategyId)
        val expected = UserStrategyKey(
            userStrategyId = userStrategyId.toString(),
            strategyClassName = "A",
            productPriceKey = ProductPriceKey(
                market = Market.BY_BIT,
                productType = ProductType.SPOT,
                productCode = "BTCUSDT",
                interval = Candle.TimeFrame.M1.timePeriod,
            ),
        )

        assertEquals(expected, actual)
    }
}

open class BaseUserStrategyPersistenceAdapterTest : BaseDbTest() {
    @Autowired
    protected lateinit var marketRepository: MarketRepositoryForStrategy

    @Autowired
    protected lateinit var productRepository: ProductRepository

    @Autowired
    protected lateinit var marketAccountRepository: MarketAccountRepository

    @Autowired
    protected lateinit var strategyRepository: StrategyRepository

    @Autowired
    protected lateinit var userStrategyRepository: UserStrategyRepository

    @Autowired
    protected lateinit var userStrategyProductRepository: UserStrategyProductRepository

    @Autowired
    protected lateinit var adapter: GetUserStrategyPersistenceAdapter

    private var index = 0

    @BeforeEach
    fun empty() = runBlocking {
        val list = adapter.getAllUserStrategies()
        assertEquals(0, list.size)
    }

    protected suspend fun setInitData(strategyClassName: String, productCodes: List<String>): Long {
        val nextIndex = index++
        val marketAccountId = marketAccountRepository.setMarketAccount(
            isProductionServer = false,
            code = Market.BY_BIT.name,
            appKey = "key$nextIndex",
            appSecret = "secret$nextIndex",
            displayName = "test",
        ).let {
            marketAccountRepository.getMarketAccountId(
                isProductionServer = false,
                code = Market.BY_BIT.name,
                appKey = "key$nextIndex",
                appSecret = "secret$nextIndex",
            )!!
        }

        val strategyId = strategyRepository.save(
            StrategyEntity(
                className = strategyClassName,
                nameKo = "테스트",
                nameEn = "test",
            )
        ).let { it.id }

        val userStrategyId = userStrategyRepository.save(
            UserStrategyEntity(
                marketAccountId = marketAccountId,
                strategyId = strategyId,
                productType = ProductType.SPOT.name,
                productCategory = ProductCategory.USER_PICK.name,
                timeFrame = Candle.TimeFrame.M1.name,
            )
        ).id

        val marketId = marketRepository.findByCode("BY_BIT")!!.id
        val productIds = productRepository.findAll()
            .filter { it.marketId == marketId && it.type == "SPOT" && productCodes.contains(it.code) }
            .map { it.id }
            .toList()

        userStrategyProductRepository.saveAll(
            productIds.mapIndexed { index, productId ->
                UserStrategyProductEntity(
                    userStrategyId = userStrategyId,
                    productId = productId,
                    sort = index + 1
                )
            }
        ).collect()

        return userStrategyId
    }
}