package com.example.sportsgd.domain.notification

/** Boundary for a future FCM implementation. The local build intentionally has no external provider. */
interface PushNotificationGateway {
    suspend fun registerDeviceToken(token: String)

    suspend fun unregisterDeviceToken(token: String)
}

class LocalOnlyPushNotificationGateway : PushNotificationGateway {
    override suspend fun registerDeviceToken(token: String) = Unit

    override suspend fun unregisterDeviceToken(token: String) = Unit
}
