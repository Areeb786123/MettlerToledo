package com.areeb.mettlertoledo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.areeb.mettlertoledo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val ACTION_USB_PERMISSION = "com.example.usbpermission"
    }

    private lateinit var usbManager: UsbManager
    private lateinit var device: UsbDevice
    private var isPermissionGranted = false
    private var isReadingWeight = false
    private lateinit var handler: Handler
    private lateinit var weightRunnable: Runnable

    lateinit var usbReceiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_USB_PERMISSION) {
                    synchronized(this) {
                        val granted =
                            intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        if (granted) {
                            val usbDevices: HashMap<String, UsbDevice>? = usbManager.deviceList
                            usbDevices?.values?.firstOrNull()?.let { device ->
                                if (usbManager.hasPermission(device)) {
                                    val weightData = retrieveWeightDataFromDevice(device)
                                    updateWeightTextView(weightData)
                                }
                            }
                        }
                    }
                }
            }
        }

        val usbDevices: HashMap<String, UsbDevice>? = usbManager.deviceList
        usbDevices?.values?.firstOrNull()?.let { device ->
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE,
            )

            registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    private fun retrieveWeightDataFromDevice(device: UsbDevice): String {
        // Implement your code to retrieve weight data from the device here
        // Replace this with your actual implementation
        return "100.5 kg" // Sample weight data
    }

    private fun updateWeightTextView(weightData: String) {
        Log.e("titis", weightData)

        binding.wightTextView.text = weightData
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
        // ...
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(usbReceiver)
    }
}
