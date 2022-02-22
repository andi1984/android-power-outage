package com.andi1984.poweroutage

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val MESSAGE_ONLINE = "Strom ist wieder da."
    val MESSAGE_OFFLINE = "Es gab einen Stromausfall!"

    private fun getBatteryStatus():Int? {
        val intent =
            this@MainActivity.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
    }

    private fun isDeviceOnAC():Boolean {
        return getBatteryStatus() == BatteryManager.BATTERY_PLUGGED_AC
    }

    private fun powerOffEvent() {
        // Send SMS that power went off
        println("POWER OFF")

        // Send SMS that power went on
        debuggerWorkflow(MESSAGE_OFFLINE)
        sendSMSWorkflow(MESSAGE_OFFLINE)
    }

    private fun powerOnEvent() {
        println("POWER ON")

        // Send SMS that power went on
        debuggerWorkflow(MESSAGE_ONLINE)
        sendSMSWorkflow(MESSAGE_ONLINE)
    }

    private fun debuggerWorkflow(message: String) {
        // Check whether checkbox is enabled
        val debuggerSwitch = findViewById<Switch>(R.id.debugger_switch)

        if(!!debuggerSwitch.isChecked) {

            // TODO: Show message in the UI
            println(message)
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val sentPI: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), 0)
        SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, sentPI, null)
    }

    private fun sendSMSWorkflow(message: String) {
        // Check whether checkbox is enabled
        val smsSwitch = findViewById<Switch>(R.id.sms_switch)

        // Get phone1, phone2
        val primaryPhoneNumber = findViewById<EditText>(R.id.phone1)
        val secondaryPhoneNumber = findViewById<EditText>(R.id.phone2)

        if(smsSwitch.isChecked) {
            println("Send SMS!!!")
            // Send SMS to both
            if(PhoneNumberUtils.isGlobalPhoneNumber(primaryPhoneNumber.text.toString())) {
                sendSMS(primaryPhoneNumber.text.toString(), message)
            }

            if(PhoneNumberUtils.isGlobalPhoneNumber(secondaryPhoneNumber.text.toString())) {
                sendSMS(secondaryPhoneNumber.text.toString(), message)
            }
        }
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
