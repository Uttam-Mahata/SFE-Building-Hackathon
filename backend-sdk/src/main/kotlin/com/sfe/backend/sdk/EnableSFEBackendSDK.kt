package com.sfe.backend.sdk

import org.springframework.context.annotation.Import
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget

/**
 * Annotation to enable SFE Backend SDK in Spring Boot applications
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(SFEBackendSDKAutoConfiguration::class)
annotation class EnableSFEBackendSDK