package com.andi1984.poweroutage

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class BatteryService : Service() {

    val TAG = BatteryService::class.simpleName
    val CHANNEL_ID = "BATTERY_SERVICE_NOTIFICATION_CHANNEL_ID"
    val CHANNEL_NAME = "BATTERY_SERVICE_NOTIFICATION_CHANNEL_NAME"
    val NOTIFICATION_ID = 13371337

    override fun onCreate() {
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(resultIntent: Intent?, resultCode: Int, startId: Int): Int {

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        notificationService.createNotificationChannel(channel)
        val notification: Notification = Notification.Builder(this, channel.id)
            .build()
        startForeground(NOTIFICATION_ID, notification)

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerStateChangeReceiver, filter)

        Log.d(TAG, "Service started")

        return startId
    }

    override fun onDestroy() {
        unregisterReceiver(powerStateChangeReceiver)
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        // TODO Auto-generated method stub
        return null
    }

    // Notifies any listeners (here the MainActivity) about power connection changes
    private var powerStateChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive")
            context?.let { intent?.let { it1 ->
                LocalBroadcastManager.getInstance(it).sendBroadcast(
                    it1
                )
            } }
        }
    }
}