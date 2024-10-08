package com.newy.algotrade.web_flux.config

import com.newy.algotrade.coroutine_based_application.common.coroutine.EventBus
import com.newy.algotrade.coroutine_based_application.common.event.CreateStrategySignalEvent
import com.newy.algotrade.coroutine_based_application.common.event.CreateUserStrategyEvent
import com.newy.algotrade.coroutine_based_application.common.event.ReceivePollingPriceEvent
import com.newy.algotrade.coroutine_based_application.common.event.SendNotificationEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class EventBusConfig {
    @Bean
    open fun createUserStrategyEventBus() = EventBus<CreateUserStrategyEvent>()

    @Bean
    open fun sendNotificationEventBus() = EventBus<SendNotificationEvent>()

    @Bean
    open fun receivePollingPriceEventBus() = EventBus<ReceivePollingPriceEvent>()

    @Bean
    open fun createStrategySignalEventBus() = EventBus<CreateStrategySignalEvent>()
}