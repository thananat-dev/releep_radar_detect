package com.axend.radarcommandsdk.connect.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import cn.com.heaton.blelibrary.ble.Ble
import cn.com.heaton.blelibrary.ble.Ble.InitCallback
import cn.com.heaton.blelibrary.ble.BleLog
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback
import cn.com.heaton.blelibrary.ble.callback.BleMtuCallback
import cn.com.heaton.blelibrary.ble.callback.BleNotifyCallback
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback
import cn.com.heaton.blelibrary.ble.model.BleDevice
import cn.com.heaton.blelibrary.ble.utils.ByteUtils
import com.axend.radarcommandsdk.connect.DeviceNetworkConnect.Companion.applicationContext
import com.axend.radarcommandsdk.connect.bean.BleMessageEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.DeviceConnectType
import com.axend.radarcommandsdk.connect.ble.handler.BleAssureDeviceIdHandler
import com.axend.radarcommandsdk.connect.ble.handler.BleDeviceIdHandler
import com.axend.radarcommandsdk.connect.ble.handler.BleDeviceOffHandler
import com.axend.radarcommandsdk.connect.ble.handler.BleDeviceServerHandler
import com.axend.radarcommandsdk.connect.ble.handler.BleDeviceWifiHandler
import com.axend.radarcommandsdk.connect.ble.handler.BleResetDeviceHandler
import com.axend.radarcommandsdk.connect.ble.handler.BleSetStateHandler
import com.axend.radarcommandsdk.connect.contract.IBleConnect
import com.axend.radarcommandsdk.connect.contract.IDeviceStatusCallback
import com.axend.radarcommandsdk.constant.STATUS_BROKEN
import com.axend.radarcommandsdk.constant.STATUS_FAILED
import com.axend.radarcommandsdk.constant.STATUS_SUCCESS
import com.axend.radarcommandsdk.utils.BleUtils
import com.axend.radarcommandsdk.utils.LogUtil
import java.util.UUID


class BleClient private constructor() : IBleConnect {

    private val BLE_SCAN_DELAYED = (10 * 1000).toLong()
    val DEVICE_BLUETOOTH_NAME = "AeroSense Wavve" 
    private var connectBleName: String = DEVICE_BLUETOOTH_NAME


    private val mBlueToothAdapter: BluetoothAdapter = BleUtils.getInstance().getBlueToolsAdapter()
    private lateinit var mBleHandlerManager: BleHandlerManager
    private var scanCallback: ScanCallback? = null
    private lateinit var mCallback: IDeviceStatusCallback

    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var bleScanHandler: Handler
    private var delayedRunnable: Runnable? = null


    private var mIsConnected: Boolean = true

    companion object {

        val instance = BleClientInstance.holder

        const val BLE_SCAN_DELAYED = 10 * 1000

    }


    private object BleClientInstance {
        val holder = BleClient()
    }


    init {
        initBleMsgHandler()
        initBlueTooth(object : Ble.InitCallback {
            override fun success() {

            }

            override fun failed(failedCode: Int) {

            }

        })
        init()

    }

    /**初始化蓝牙*/
    private fun initBlueTooth(callBack: Ble.InitCallback) {
        Ble.options().apply {
            logBleEnable = true 
            throwBleException = true 
            logTAG = "AndroidBLE"
            autoConnect = false 
            isIgnoreRepeat = false 
            connectFailedRetryCount = 3 
            connectTimeout = BLE_SCAN_DELAYED as Long
            scanPeriod = 12 * 1000  
            maxConnectNum = 7 
            uuidService = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E") 
            uuidWriteCha= UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E") 
//            uuidReadCha=UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E") 
            uuidNotifyCha =
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") 


        }.create<BleDevice>(applicationContext, callBack)

    }


    private fun init() {
        if (scanCallback == null) {
            scanCallback = object : ScanCallback() {
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    LogUtil.i("scan err code :$errorCode")
                }


                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    if (ActivityCompat.checkSelfPermission(
                            applicationContext!!,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    if (connectBleName.equals(result?.device?.name, ignoreCase = true)) {
                        stopScan()
                        if (Ble.getInstance<BleDevice>().bleRequest != null) {
                            connectBle(result?.device)
                        } else {
                            initBlueTooth(object : InitCallback {
                                override fun success() {
                                    BleLog.e("MainApplication", "init success")
                                    LogUtil.i("init success")
                                    connectBle(result?.device)
                                }

                                override fun failed(failedCode: Int) {
                                    BleLog.e("MainApplication", "init fail: $failedCode")
                                    LogUtil.i("init fail: $failedCode")
                                }
                            })
                        }
                    }
                    LogUtil.i("bluetooth scaning：${result?.device?.name}")
                }

            }

        }
        if (delayedRunnable == null) {
            delayedRunnable = Runnable {
                stopScan()
                LogUtil.d("time out")
                callbackState(STATUS_FAILED)
            }
        }
    }

    private fun connectBle(device: BluetoothDevice?) {
        if (device == null) return
        try {
            Ble.getInstance<BleDevice>().connect(device.address, blueToothConnectCallback)
        } catch (e: java.lang.Exception) {
            LogUtil.d("connectBle ---->err")
        }
    }


    /**初始化蓝牙指令消息管理*/
    private fun initBleMsgHandler() {
        mBleHandlerManager = BleHandlerManager()
        mBleHandlerManager.addHandler(BleResetDeviceHandler())
        mBleHandlerManager.addHandler(BleDeviceIdHandler())
        mBleHandlerManager.addHandler(BleAssureDeviceIdHandler())
        mBleHandlerManager.addHandler(BleDeviceWifiHandler())
        mBleHandlerManager.addHandler(BleDeviceServerHandler())
        mBleHandlerManager.addHandler(BleSetStateHandler())
        mBleHandlerManager.addHandler(BleDeviceOffHandler())
    }


    /***/

    private val blueToothConnectCallback: BleConnectCallback<BleDevice> = object :
        BleConnectCallback<BleDevice>() {
        override fun onConnectionChanged(device: BleDevice?) {
            if (device?.isConnected == true) {
                mIsConnected = true
            } else if (device?.isDisconnected == true) {
                mIsConnected = false
                Ble.getInstance<BleDevice>().disconnectAll()
                callbackState(STATUS_BROKEN)
            }
        }

        override fun onConnectFailed(device: BleDevice?, errorCode: Int) {
            super.onConnectFailed(device, errorCode)
            Ble.getInstance<BleDevice>().disconnectAll()
            callbackState(STATUS_FAILED)
        }


        override fun onConnectCancel(device: BleDevice?) {
            super.onConnectCancel(device)
            Ble.getInstance<BleDevice>().disconnectAll()
        }


        override fun onServicesDiscovered(device: BleDevice?, gatt: BluetoothGatt?) {
            super.onServicesDiscovered(device, gatt)
            if (gatt != null) {
                bluetoothGatt = gatt
            }
        }

        override fun onReady(device: BleDevice?) {
            super.onReady(device)
            Ble.getInstance<BleDevice>()
                .enableNotify(device, true, object : BleNotifyCallback<BleDevice>() {
                    override fun onChanged(
                        device: BleDevice,
                        characteristic: BluetoothGattCharacteristic
                    ) {
                        val uuid = characteristic.uuid
                        val value = characteristic.value
                        LogUtil.d(
                            String.format(
                                "recive data: %s",
                                ByteUtils.toHexString(value)
                            )
                        )
                        handlerBleMsg(value)
                        LogUtil.d("onChanged==uuid:$uuid")
                        LogUtil.d("onChanged==data:" + ByteUtils.toHexString(characteristic.value))
                    }

                    override fun onNotifySuccess(device: BleDevice) {
                        super.onNotifySuccess(device)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Ble.getInstance<BleDevice>().setMTU(
                                device.bleAddress,
                                100,
                                object : BleMtuCallback<BleDevice?>() {
                                    override fun onMtuChanged(
                                        device: BleDevice,
                                        mtu: Int,
                                        status: Int
                                    ) {
                                        super.onMtuChanged(device, mtu, status)
                                        callbackState(STATUS_SUCCESS)
                                    }
                                })
                        }
                    }
                })
        }


    }

    private fun handlerBleMsg(data: ByteArray) {
        val notifyEntity: BleMsgEntity? = mBleHandlerManager.decode(data)
        if (notifyEntity != null) {
            callbackReadDate(notifyEntity)
        }else {
            callbackReadDate("")
        }

    }

    private fun bleScan() {
        bleScanHandler = Handler(Looper.getMainLooper())
        delayedRunnable?.let { bleScanHandler.postDelayed(it, BLE_SCAN_DELAYED) }
        if (ActivityCompat.checkSelfPermission(
                applicationContext!!,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LogUtil.i("bluetooth scan start")
        mBlueToothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
    }

    private fun stopScan() {
        LogUtil.d("bluetooth scan stop")
        val ble = Ble.getInstance<BleDevice>()
        if (ble.isScanning) {
            ble.stopScan()
        }
        if (scanCallback != null) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext!!,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mBlueToothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
        if (bleScanHandler != null && delayedRunnable != null) {
            bleScanHandler.removeCallbacks(delayedRunnable!!)
        }
    }


    override fun connect() {
        bleScan()
    }

    override fun setCallback(statusCallback: IDeviceStatusCallback?) {
        if (statusCallback != null) {
            mCallback = statusCallback
        }
    }

    override fun isConnect(): Boolean {
        return mIsConnected
    }

    override fun close() {
        if (mBlueToothAdapter != null && bluetoothGatt != null) {
            LogUtil.d("disconnect close")
            if (ActivityCompat.checkSelfPermission(
                    applicationContext!!,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothGatt.disconnect()
            bluetoothGatt.close()
        }
        try {
            Ble.getInstance<BleDevice>().cancelAutoConnects()
            Ble.getInstance<BleDevice>()
                .cancelConnectings(Ble.getInstance<BleDevice>().connectedDevices)
            Ble.getInstance<BleDevice>().disconnectAll()
            Ble.getInstance<BleDevice>().released()
        } catch (e: Exception) {
            LogUtil.w("close<<<bleth<<err")
        }
    }

    override fun sendMsg(data: Any?) {
        if (data is BleMessageEntity) {
            val message: ByteArray? = data.getMessage()
            writeData(message)
        } else if (data is ByteArray) {
            writeData(data as ByteArray?)
        } else if (data is BleMsgEntity) {
            val writeBleMsg: ByteArray? = mBleHandlerManager.getWriteBleMsg(data as BleMsgEntity?)
            writeData(writeBleMsg)
        }
    }

    override fun getConnectType(): DeviceConnectType? {
        TODO("Not yet implemented")
    }

    override fun setDeviceName(deviceName: String?) {
        if (deviceName != null) {
            this.connectBleName = deviceName
        }
    }


    private fun writeData(data: ByteArray?) {
        try {
            if (Ble.getInstance<BleDevice?>().connectedDevices[0] == null) {
                return
            }
            Ble.getInstance<BleDevice>().write(
                Ble.getInstance<BleDevice>().connectedDevices[0],
                data,
                object : BleWriteCallback<BleDevice?>() {
                    override fun onWriteSuccess(
                        device: BleDevice?,
                        characteristic: BluetoothGattCharacteristic
                    ) {
                        LogUtil.d("write data ${ByteUtils.toHexString(data)}")
                    }

                    override fun onWriteFailed(device: BleDevice?, failedCode: Int) {
                        super.onWriteFailed(device, failedCode)
                        LogUtil.d("write data failed $failedCode")
                    }
                })
        } catch (e: java.lang.Exception) {
            LogUtil.d("write data failed")
        }
    }


    private fun callbackState(state: Int) {
        if (null != mCallback) {
            mCallback.callBackDeviceStatus(state)
        } else {
            LogUtil.d("not set callback！")
        }
    }

    private fun callbackReadDate(data: Any) {
        LogUtil.d(buildString {
            append("data size：").append(data)
        })
        if (mCallback == null) return
        mCallback.callBackDeviceData(data as Object)
    }

}