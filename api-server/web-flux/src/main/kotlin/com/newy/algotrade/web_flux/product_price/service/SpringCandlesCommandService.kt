package com.newy.algotrade.web_flux.product_price.service

import com.newy.algotrade.coroutine_based_application.product_price.port.`in`.ProductPriceQuery
import com.newy.algotrade.coroutine_based_application.product_price.port.out.CandlePort
import com.newy.algotrade.coroutine_based_application.product_price.service.CandlesCommandService
import org.springframework.stereotype.Service

@Service
class SpringCandlesCommandService(
    productPriceQuery: ProductPriceQuery,
    candlePort: CandlePort
) : CandlesCommandService(productPriceQuery, candlePort)