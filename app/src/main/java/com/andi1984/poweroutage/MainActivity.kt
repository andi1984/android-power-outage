package com.andi1984.poweroutage

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MainActivity : AppCompatActivity() {
    val MESSAGE_ONLINE = "Strom ist wieder da."
    val MESSAGE_OFFLINE = "Es gab einen Stromausfall!"

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
        println("Send sms to '$phoneNumber'")
        val sentPI: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), 0)
        val deliveredPI: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_DELIVERED"), 0)
        val SENT = "SMS_SENT"
        val DELIVERED = "SMS_DELIVERED"

        this@MainActivity.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(arg0: Context, arg1: Intent) {
                    when (resultCode) {
                        RESULT_OK -> {}
                        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                            println("SMS - Generic failure")
                        }
                        SmsManager.RESULT_ERROR_NO_SERVICE -> {
                            println("SMS - No Service")
                        }
                        SmsManager.RESULT_ERROR_NULL_PDU -> {
                            println("SMS - Null PDU")
                        }
                        SmsManager.RESULT_ERROR_RADIO_OFF -> {
                            println("Radio off")
                        }
                    }
                }
            }, IntentFilter(SENT)
        )
        // ---when the SMS has been delivered---
        // ---when the SMS has been delivered---
        this@MainActivity.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(arg0: Context, arg1: Intent) {
                    when (resultCode) {
                        RESULT_OK -> {
                            println("SMS - OK")
                        }
                        RESULT_CANCELED -> {
                            println("SMS - Canceled")
                        }
                    }
                }
            }, IntentFilter(DELIVERED)
        )

        // TODO: Understand whether this works or not?
        this@MainActivity.getSystemService(SmsManager::class.java).sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI)
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
            println("${primaryPhoneNumber.text.toString()} is valid? ${PhoneNumberUtils.isGlobalPhoneNumber(primaryPhoneNumber.text.toString())}")
            if(PhoneNumberUtils.isGlobalPhoneNumber(primaryPhoneNumber.text.toString())) {
                sendSMS(primaryPhoneNumber.text.toString(), message)
            }

            if(PhoneNumberUtils.isGlobalPhoneNumber(secondaryPhoneNumber.text.toString())) {
                sendSMS(secondaryPhoneNumber.text.toString(), message)
            }
        }
    }

    var wasDeviceOnAC:Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val checkChargeButton = findViewById<Button>(R.id.check_charge_button)
        checkChargeButton.setOnClickListener {
            updateStatus(this)
        }

        updateStatus(this)

        Intent(this, BatteryService::class.java).also { intent ->
            startForegroundService(intent)
        }

        val localReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context?.let {
                    updateStatus(context)
                }
            }
        }

        // Listen for updates from the service via local broadcast manager
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter)
   }

    fun updateStatus(context: Context){
        val isDeviceOnAC = BatteryMonitor().isDeviceOnAC(context)

        val isChargingLabel = findViewById<TextView>(R.id.is_charging_label)
        isChargingLabel.setText(if (isDeviceOnAC) "Power connected" else "Power disconnected")

        if ( wasDeviceOnAC == null ) {
            wasDeviceOnAC = isDeviceOnAC
            return
        }

        wasDeviceOnAC?.let {
            if( isDeviceOnAC != it) {
                if(!it && isDeviceOnAC) {
                    // Power went on -->
                    powerOnEvent()
                } else {
                    // Power went off -->
                    powerOffEvent()
                }

                // Update last device state
                wasDeviceOnAC = isDeviceOnAC
            } else {
                println("nothing changed")
            }
        }
    }
}
