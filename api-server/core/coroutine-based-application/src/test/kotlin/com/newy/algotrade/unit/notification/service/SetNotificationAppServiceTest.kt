package com.newy.algotrade.unit.notification.service

import com.newy.algotrade.coroutine_based_application.notification.port.`in`.model.SetNotificationAppCommand
import com.newy.algotrade.coroutine_based_application.notification.port.out.NotificationAppPort
import com.newy.algotrade.coroutine_based_application.notification.service.SetNotificationAppService
import com.newy.algotrade.domain.common.consts.NotificationApp
import com.newy.algotrade.domain.common.exception.DuplicateDataException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

open class NoErrorNotificationAppAdapter : NotificationAppPort {
    override suspend fun hasNotificationApp(userId: Long) = false
    override suspend fun setNotificationApp(command: SetNotificationAppCommand) = true
}

private val command = SetNotificationAppCommand(
    userId = 1,
    type = NotificationApp.SLACK,
    url = NotificationApp.SLACK.host
)

class FailedSetNotificationAppServiceTest {
    @Test
    fun `이미 알림 앱을 등록한 경우`() = runTest {
        val alreadyExistsAdapter = object : NoErrorNotificationAppAdapter() {
            override suspend fun hasNotificationApp(userId: Long) = true
        }
        val service = SetNotificationAppService(alreadyExistsAdapter)

        try {
            service.setNotificationApp(command)
            fail()
        } catch (e: DuplicateDataException) {
            assertEquals("이미 알림 앱을 등록했습니다.", e.message)
        }
    }
}

@DisplayName("port 호출 순서 확인")
class SuccessSetNotificationAppServiceTest : NoErrorNotificationAppAdapter() {
    private var log = ""

    @Test
    fun test() = runTest {
        val service = SetNotificationAppService(this@SuccessSetNotificationAppServiceTest)
        val isRegistered = service.setNotificationApp(command)

        assertTrue(isRegistered)
        assertEquals("hasNotificationAppPort setNotificationAppPort ", log)
    }

    override suspend fun hasNotificationApp(userId: Long): Boolean {
        log += "hasNotificationAppPort "
        return super.hasNotificationApp(userId)
    }

    override suspend fun setNotificationApp(command: SetNotificationAppCommand): Boolean {
        log += "setNotificationAppPort "
        return super.setNotificationApp(command)
    }
}