package com.newy.algotrade.web_flux.product.application.service

import com.newy.algotrade.coroutine_based_application.product.application.service.RunStrategyService
import com.newy.algotrade.coroutine_based_application.product.port.out.GetCandlePort
import com.newy.algotrade.coroutine_based_application.product.port.out.GetStrategyPort
import com.newy.algotrade.coroutine_based_application.product.port.out.OnCreatedStrategySignalPort
import com.newy.algotrade.coroutine_based_application.product.port.out.StrategySignalHistoryPort
import org.springframework.stereotype.Service

@Service
class RunStrategySpringService(
    candlePort: GetCandlePort,
    strategyPort: GetStrategyPort,
    strategySignalHistoryPort: StrategySignalHistoryPort,
    onCreatedStrategySignalPort: OnCreatedStrategySignalPort
) : RunStrategyService(candlePort, strategyPort, strategySignalHistoryPort, onCreatedStrategySignalPort)