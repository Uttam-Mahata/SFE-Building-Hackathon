package com.sfe.paymentbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentBackendApplication

fun main(args: Array<String>) {
    runApplication<PaymentBackendApplication>(*args)
}
