package com.newy.algotrade.unit.user_strategy.port.`in`.model

import com.newy.algotrade.coroutine_based_application.user_strategy.port.`in`.model.SetUserStrategyCommand
import com.newy.algotrade.domain.chart.Candle
import com.newy.algotrade.domain.common.consts.ProductCategory
import com.newy.algotrade.domain.common.consts.ProductType
import com.newy.algotrade.domain.user_strategy.SetUserStrategy
import com.newy.algotrade.domain.user_strategy.SetUserStrategyKey
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows


val dto = SetUserStrategyCommand(
    marketAccountId = 1,
    strategyClassName = "BuyTripleRSIStrategy",
    productCategory = ProductCategory.USER_PICK,
    productType = ProductType.SPOT,
    productCodes = listOf("BTCUSDT"),
    timeFrame = Candle.TimeFrame.M1,
)

class SetUserStrategyCommandTest {
    @Test
    fun `marketAccountId 은 0 이상 이어야 한다`() {
        assertThrows<ConstraintViolationException> { dto.copy(marketAccountId = -1) }
        assertThrows<ConstraintViolationException> { dto.copy(marketAccountId = 0) }
        assertDoesNotThrow {
            dto.copy(marketAccountId = 1)
            dto.copy(marketAccountId = 2)
        }
    }

    @Test
    fun `strategyClassName 는 NotBlank 이어야 한다`() {
        assertThrows<ConstraintViolationException> { dto.copy(strategyClassName = "") }
        assertThrows<ConstraintViolationException> { dto.copy(strategyClassName = " ") }
        assertDoesNotThrow {
            dto.copy(strategyClassName = "NotBlankString")
        }
    }

    @Test
    fun `productCodes 의 item 들은 NotBlank 이어야 한다`() {
        assertThrows<ConstraintViolationException> {
            dto.copy(productCodes = listOf(""))
            dto.copy(productCodes = listOf(" ", ""))
        }
    }

    @Test
    fun `productCategory 값이 'USER_PICK' 인 경우, productCodes 는 1개 이상이여야 한다`() {
        assertThrows<IllegalArgumentException> {
            dto.copy(
                productCategory = ProductCategory.USER_PICK,
                productCodes = emptyList()
            )
        }
        assertDoesNotThrow {
            dto.copy(
                productCategory = ProductCategory.USER_PICK,
                productCodes = listOf("BTCUSDT")
            )
        }
    }

    @Test
    fun `productCategory 값이 'TOP_TRADING_VALUE' 인 경우, productCodes 는 emptyList 여야 한다`() {
        assertThrows<IllegalArgumentException> {
            dto.copy(
                productCategory = ProductCategory.TOP_TRADING_VALUE,
                productCodes = listOf("BTCUSDT")
            )
        }
        assertDoesNotThrow {
            dto.copy(
                productCategory = ProductCategory.TOP_TRADING_VALUE,
                productCodes = emptyList()
            )
        }
    }

    @Test
    fun toDomainEntity() {
        assertEquals(
            SetUserStrategy(
                SetUserStrategyKey(
                    marketServerAccountId = 1,
                    strategyClassName = "BuyTripleRSIStrategy",
                    productType = ProductType.SPOT,
                ),
                productCategory = ProductCategory.USER_PICK,
                timeFrame = Candle.TimeFrame.M1,
            ),
            SetUserStrategyCommand(
                marketAccountId = 1,
                strategyClassName = "BuyTripleRSIStrategy",
                productType = ProductType.SPOT,
                productCategory = ProductCategory.USER_PICK,
                productCodes = listOf("BTCUSDT"),
                timeFrame = Candle.TimeFrame.M1,
            ).toDomainEntity()
        )
    }
}