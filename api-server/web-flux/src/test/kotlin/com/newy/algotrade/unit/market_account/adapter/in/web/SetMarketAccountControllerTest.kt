package com.newy.algotrade.unit.market_account.adapter.`in`.web

import com.newy.algotrade.coroutine_based_application.market_account.port.`in`.model.SetMarketAccountCommand
import com.newy.algotrade.domain.common.consts.GlobalEnv
import com.newy.algotrade.domain.common.consts.Market
import com.newy.algotrade.domain.market_account.MarketAccount
import com.newy.algotrade.domain.market_account.MarketServer
import com.newy.algotrade.web_flux.market_account.adapter.`in`.web.SetMarketAccountController
import com.newy.algotrade.web_flux.market_account.adapter.`in`.web.model.SetMarketAccountRequest
import helpers.TestEnv
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SetMarketAccountControllerTest {
    @Test
    fun `requestModel 을 incomingPortModel 로 변경하기`() = runBlocking {
        GlobalEnv.initializeAdminUserId(TestEnv.TEST_ADMIN_USER_ID)

        var incomingPortModel: SetMarketAccountCommand? = null
        val controller = SetMarketAccountController(
            setMarketAccountUseCase = { marketAccount ->
                fakeDomainEntity.also {
                    incomingPortModel = marketAccount
                }
            }
        )

        controller.setMarketAccount(
            SetMarketAccountRequest(
                market = "LS_SEC",
                isProduction = true,
                displayName = "name",
                appKey = "key",
                appSecret = "secret",
            )
        )

        assertEquals(
            SetMarketAccountCommand(
                userId = TestEnv.TEST_ADMIN_USER_ID,
                market = Market.LS_SEC,
                isProduction = true,
                displayName = "name",
                appKey = "key",
                appSecret = "secret",
            ),
            incomingPortModel
        )
    }
}

private val fakeDomainEntity = MarketAccount(
    id = 10,
    userId = 1,
    marketServer = MarketServer(
        id = 100,
        marketId = 1000
    ),
    displayName = "displayName",
    appKey = "appKey",
    appSecret = "appSecret",
)