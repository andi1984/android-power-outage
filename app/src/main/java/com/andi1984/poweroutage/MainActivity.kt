package com.andi1984.poweroutage

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val intent =
            this@MainActivity.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        val alertDialog: android.app.AlertDialog? = android.app.AlertDialog.Builder(this) //set icon
            .setIcon(android.R.drawable.ic_dialog_alert) //set title
            .setTitle("Battery Extra Plugged") //set message
            .setMessage("${plugged}") //set positive button
            .setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialogInterface, i -> //set what would happen when positive button is clicked
                    finish()
                }) //set negative button
            .show()
    }
}
