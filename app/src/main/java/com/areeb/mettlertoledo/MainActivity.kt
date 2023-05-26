package com.areeb.mettlertoledo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.areeb.mettlertoledo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding
    private val binding get() = _binding

    companion object {
        private const val ACTION_USB_PERMISSION = "com.areeb.mettlertoledo.USB_PERMISSION"
    }

    private lateinit var usbManager: UsbManager
    private var usbDevice: UsbDevice? = null
    private lateinit var usbConnection: UsbDeviceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Register a broadcast receiver to handle USB permission request
        val usbPermissionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (ACTION_USB_PERMISSION == action) {
                    synchronized(this) {
                        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                // Permission granted, open the connection
                                usbDevice = device
                                openConnection()
                            }
                        } else {
                            // Permission denied
                            Toast.makeText(
                                this@MainActivity,
                                "Permission denied for USB device",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
        }

        // Register the broadcast receiver to receive USB permission events
        val permissionFilter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbPermissionReceiver, permissionFilter)

        // Request permission for the USB device
        val devices = usbManager.deviceList.values
        if (devices.isNotEmpty()) {
            usbDevice = devices.first()
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            usbManager.requestPermission(usbDevice, permissionIntent)
        } else {
            // No USB devices found
            Toast.makeText(this, "No USB devices found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openConnection() {
        usbConnection = usbManager.openDevice(usbDevice)
        if (usbConnection != null && usbDevice != null) {
            // Claim the interface
            val usbInterface = usbDevice!!.getInterface(0)
            usbConnection.claimInterface(usbInterface, true)

            // Configure the interface and endpoints
            val endpoint = usbInterface.getEndpoint(0)
            // endpoint may need to be determined based on your device's specifications

            // Read data from the endpoint
            val buffer = ByteArray(endpoint.maxPacketSize)
            val bytesRead = usbConnection.bulkTransfer(endpoint, buffer, buffer.size, 0)

            if (bytesRead > 0) {
                // Process the received data
                val receivedData = String(buffer, 0, bytesRead)
                // TODO: Handle the received data
                Log.e("jao", receivedData)
                binding.wightTextView.text = receivedData
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release the USB connection
        if (usbConnection != null) {
            usbConnection.releaseInterface(usbDevice!!.getInterface(0))
            usbConnection.close()
        }
    }
}
