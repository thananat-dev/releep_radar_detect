package com.axend.radarcommandsdk.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import com.axend.radarcommandsdk.connect.DeviceNetworkConnect
import com.axend.radarcommandsdk.connect.DeviceNetworkConnect.Companion.applicationContext

class BleUtils private constructor() {

    private var blueToolsAdapter: BluetoothAdapter? =null

    init {
        if (blueToolsAdapter == null) {
            blueToolsAdapter =
                (applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            blueToolsAdapter ?: LogUtil.e("no blue enable")
        }
    }

    companion object {

        @Volatile
        private var instance: BleUtils? = null


        fun getInstance() = instance ?: synchronized(this) {
            instance ?: BleUtils().also { instance = it }
        }

    }



    fun getBlueToolsAdapter(): BluetoothAdapter = blueToolsAdapter!!


    fun isOpenBle(): Boolean = blueToolsAdapter!!.isEnabled


    fun getBleScanner(): BluetoothLeScanner = blueToolsAdapter!!.bluetoothLeScanner


}