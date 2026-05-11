package com.binahr.service


import com.binahr.BuildConfig
import com.binahr.data.api.TokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BinahrFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        TokenManager.fcmToken = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // FCM push is handled by the OS notification tray automatically
        // when the app is in background. For foreground, show a local notification
        // if needed in future.
    }
}
