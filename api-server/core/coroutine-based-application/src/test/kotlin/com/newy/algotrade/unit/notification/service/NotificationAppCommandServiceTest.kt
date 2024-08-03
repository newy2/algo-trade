package com.newy.algotrade.unit.notification.service

import com.newy.algotrade.coroutine_based_application.notification.port.`in`.model.SetNotificationAppCommand
import com.newy.algotrade.coroutine_based_application.notification.port.out.HasNotificationAppPort
import com.newy.algotrade.coroutine_based_application.notification.port.out.NotificationAppPort
import com.newy.algotrade.coroutine_based_application.notification.port.out.SetNotificationAppPort
import com.newy.algotrade.coroutine_based_application.notification.service.NotificationAppCommandService
import com.newy.algotrade.domain.common.consts.NotificationAppType
import com.newy.algotrade.domain.common.exception.DuplicateDataException
import com.newy.algotrade.domain.notification.NotificationApp
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

private val incomingPortModel = SetNotificationAppCommand(
    userId = 1,
    type = NotificationAppType.SLACK,
    url = NotificationAppType.SLACK.host
)

@DisplayName("예외사항 테스트")
class NotificationAppCommandServiceExceptionTest {
    @Test
    fun `이미 알림 앱을 등록한 경우`() = runTest {
        val alreadySavedNotificationAppAdapter = HasNotificationAppPort { _ -> true }
        val service = NotificationAppCommandService(
            setNotificationAppPort = NoErrorNotificationAppAdapter(),
            hasNotificationAppPort = alreadySavedNotificationAppAdapter,
        )

        try {
            service.setNotificationApp(incomingPortModel)
            fail()
        } catch (e: DuplicateDataException) {
            assertEquals("이미 알림 앱을 등록했습니다.", e.message)
        }
    }
}

@DisplayName("해피패스 테스트")
class NotificationAppCommandServiceTest {
    @Test
    fun `저장 성공한 경우`() = runTest {
        val successSavedNotificationAppAdapter = SetNotificationAppPort { _ -> true }
        val service = NotificationAppCommandService(
            hasNotificationAppPort = NoErrorNotificationAppAdapter(),
            setNotificationAppPort = successSavedNotificationAppAdapter,
        )

        assertTrue(service.setNotificationApp(incomingPortModel))
    }

    @Test
    fun `저장 실패한 경우`() = runTest {
        val failedSavedNotificationAppAdapter = SetNotificationAppPort { _ -> false }
        val service = NotificationAppCommandService(
            hasNotificationAppPort = NoErrorNotificationAppAdapter(),
            setNotificationAppPort = failedSavedNotificationAppAdapter,
        )

        assertFalse(service.setNotificationApp(incomingPortModel))
    }
}

open class NoErrorNotificationAppAdapter : NotificationAppPort {
    override suspend fun hasNotificationApp(userId: Long) = false
    override suspend fun setNotificationApp(domainEntity: NotificationApp) = true
}