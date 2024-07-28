package com.newy.algotrade.integration.market_account.adapter.out.persistence

import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.market_account.MarketServer
import com.newy.algotrade.domain.market_account.SetMarketAccount
import com.newy.algotrade.web_flux.market_account.adapter.out.persistent.MarketAccountPersistenceAdapter
import com.newy.algotrade.web_flux.market_account.adapter.out.persistent.repository.MarketAccountRepository
import com.newy.algotrade.web_flux.market_account.adapter.out.persistent.repository.MarketServerRepository
import helpers.BaseDbTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle

@DisplayName("MarketServer 조회하기")
class MarketServerTest(
    @Autowired private val marketServerRepository: MarketServerRepository,
    @Autowired private val adapter: MarketAccountPersistenceAdapter
) : BaseDbTest() {
    @Test
    fun `저장되지 않은 MarketServer 조회하기`() = runTransactional {
        marketServerRepository.deleteAll()
        assertNull(
            adapter.getMarketServer(
                market = Market.BY_BIT,
                isProductionServer = true
            )
        )
    }

    @Test
    fun `저장된 MarketServer 조회하기`() = runBlocking {
        assertNotNull(
            adapter.getMarketServer(
                market = Market.BY_BIT,
                isProductionServer = true
            )
        )
    }
}

@DisplayName("MarketAccount 등록 테스트")
class SaveMarketAccountTest(
    @Autowired private val adapter: MarketAccountPersistenceAdapter
) : BaseMarketAccountPersistenceAdapterTest() {
    private lateinit var domainEntity: SetMarketAccount

    @BeforeEach
    fun setUp(): Unit = runBlocking {
        domainEntity = SetMarketAccount(
            userId = getAdminUserId(),
            marketServer = adapter.getMarketServer(
                market = Market.BY_BIT,
                isProductionServer = true
            )!!,
            displayName = "displayName",
            appKey = "appKey",
            appSecret = "appSecret",
        )
    }

    @Test
    fun `MarketAccount 등록여부 조회하기`() = runBlocking {
        assertFalse(adapter.hasMarketAccount(domainEntity))
    }

    @Test
    fun `MarketAccount 등록하기`() = runTransactional {
        val isSaved = adapter.saveMarketAccount(domainEntity)

        assertTrue(isSaved)
        assertTrue(adapter.hasMarketAccount(domainEntity))
    }
}

@DisplayName("중복 데이터 등록 테스트")
class DuplicateExceptionTest(
    @Autowired private val marketAccountRepository: MarketAccountRepository,
    @Autowired private val adapter: MarketAccountPersistenceAdapter
) : BaseMarketAccountPersistenceAdapterTest() {
    private lateinit var byBitServer: MarketServer
    private lateinit var lsSecServer: MarketServer
    private lateinit var domainEntity: SetMarketAccount

    @BeforeEach
    fun setUp(): Unit = runBlocking {
        byBitServer = adapter.getMarketServer(
            market = Market.BY_BIT,
            isProductionServer = true
        )!!
        lsSecServer = adapter.getMarketServer(
            market = Market.LS_SEC,
            isProductionServer = true
        )!!
        domainEntity = SetMarketAccount(
            userId = getAdminUserId(),
            marketServer = byBitServer,
            displayName = "displayName",
            appKey = "appKey",
            appSecret = "appSecret",
        )
        adapter.saveMarketAccount(domainEntity)
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
        marketAccountRepository.deleteAll()
    }

    @Test
    fun `중복 데이터를 저장하는 경우`() = runBlocking {
        try {
            adapter.saveMarketAccount(domainEntity)
            fail()
        } catch (e: DuplicateKeyException) {
            assertTrue(true)
        }
    }

    @Test
    fun `unique index 는 market_server_id, app_key, app_secret 컬럼에 걸려 있음`() = runBlocking {
        val notDuplicateEntity1 = domainEntity.copy(marketServer = lsSecServer)
        val notDuplicateEntity2 = domainEntity.copy(appKey = "different ${domainEntity.appKey}")
        val notDuplicateEntity3 = domainEntity.copy(appSecret = "different ${domainEntity.appSecret}")

        assertTrue(adapter.saveMarketAccount(notDuplicateEntity1))
        assertTrue(adapter.saveMarketAccount(notDuplicateEntity2))
        assertTrue(adapter.saveMarketAccount(notDuplicateEntity3))
    }
}

open class BaseMarketAccountPersistenceAdapterTest : BaseDbTest() {
    @Autowired
    private lateinit var databaseClient: DatabaseClient

    protected suspend fun getAdminUserId(): Long {
        // TODO UserRepository 구현 시, 리팩토링 하기
        val adminUser = databaseClient
            .sql(
                """
                SELECT id
                FROM   users
                WHERE  email = 'admin'
            """.trimIndent()
            )
            .fetch()
            .awaitSingle()

        return adminUser["id"] as Long
    }
}