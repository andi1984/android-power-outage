package com.andi1984.poweroutage

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private fun getBatteryStatus():Int? {
        val intent =
            this@MainActivity.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
    }

    private fun isDeviceOnAC():Boolean {
        return getBatteryStatus() == BatteryManager.BATTERY_PLUGGED_AC
    }

    private fun powerOffEvent() {
        //TODO: Send SMS that power went off

        println("POWER OFF")
    }

    private fun powerOnEvent() {
        // TODO: Send SMS that power went on

        println("POWER ON")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var wasDeviceOnAC = isDeviceOnAC()

        val scheduledEx = Executors.newScheduledThreadPool(1)

        val printStatus = Runnable {
            val isDeviceCurrentlyOnAC = isDeviceOnAC()
            if(isDeviceCurrentlyOnAC != wasDeviceOnAC) {
                if(!wasDeviceOnAC && isDeviceCurrentlyOnAC) {
                    // Power went on -->
                    powerOnEvent()
                } else {
                    // Power went off -->
                    powerOffEvent()
                }

                // Update last device state
                wasDeviceOnAC = isDeviceCurrentlyOnAC
            } else {
                println("nothing changed")
            }
        }

        scheduledEx.scheduleAtFixedRate(printStatus, 0, 10, TimeUnit.SECONDS)
   }
}
