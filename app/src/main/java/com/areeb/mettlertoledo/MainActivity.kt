package com.areeb.mettlertoledo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.areeb.mettlertoledo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding
    private val binding get() = _binding
    private lateinit var usbManager: UsbManager
    private lateinit var usbReceiver: BroadcastReceiver

    companion object {
        private const val ACTION_USB_PERMISSION = "com.areeb.mettlertoledo.USB_PERMISSION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = _binding.root
        setContentView(view)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_USB_PERMISSION) {
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (granted && device != null) {
                        // Permission granted, read data from the USB device
                        val data = readDataFromUsbDevice(device)
                        binding.wightTextView.text = data
                        Log.e("dataXXX", data)
                    }
                }
            }
        }

        // Register the USB receiver to listen for USB permission broadcasts
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        // Request permission to access the USB device
        val usbDevices: HashMap<String, UsbDevice>? = usbManager.deviceList
        usbDevices?.values?.forEach { device ->
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun readDataFromUsbDevice(device: UsbDevice): String {
        // Implement your code to read data from the USB device here
        // Return the data as a String
        // Example: return "Data read from USB device"
        // Replace this example with your actual code to read data from the USB device

        // Simulating data read for demonstration purposes
        val simulatedData = device.toString()
        return simulatedData
    }
}
