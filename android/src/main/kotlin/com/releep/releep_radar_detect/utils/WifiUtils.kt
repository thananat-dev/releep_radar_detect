package com.axend.radarcommandsdk.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build


class WifiUtils private constructor(private var context: Context) : BroadcastReceiver() {
    private val mWifiManager: WifiManager = context
        .getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val mWifiInfo: WifiInfo = mWifiManager.connectionInfo


    companion object {
        @Volatile
        var instance: WifiUtils? = null

        fun getInstance(context: Context): WifiUtils {
            if (instance == null) {
                synchronized(WifiUtils::class) {
                    if (instance == null) {
                        instance = WifiUtils(context)
                    }
                }
            }
            return instance!!
        }
    }


    fun getWifiSSID(): String? {
        var ssid: String? = ""
        mWifiInfo?.let {
            ssid = it.ssid
            ssid = ssid?.let {
                it.substring(1, it.length - 1)
            }
        }

        return ssid

    }

    fun openWifi(): Boolean {
        var mOpen = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mOpen = mWifiManager.isWifiEnabled
        } else {
            if (!mWifiManager.isWifiEnabled) {
                mOpen = mWifiManager.setWifiEnabled(true)
            }
        }
        return mOpen
    }

    fun wifiStatus(): Boolean = mWifiManager.isWifiEnabled


    fun is5GHz(freq: Int): Boolean = freq in 4901..5899


    fun closeWifi() {
        if (mWifiManager.isWifiEnabled) {
            mWifiManager.setWifiEnabled(false)
        }
    }


    fun startScanWifi(): Boolean {
        var isContinue = false
        mWifiManager?.let {
            isContinue = it.startScan()
        }
        return isContinue
    }


    @SuppressLint("MissingPermission")
    fun getScanWifiResult(): List<ScanResult> {
        var resultList = listOf<ScanResult>()
        mWifiManager?.let {
            resultList = it.scanResults
        }
        return resultList
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        TODO("Not yet implemented")
    }

}