package com.axend.radarcommandsdk.connect

import android.app.Activity
import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.bean.DeviceConnectType
import com.axend.radarcommandsdk.connect.bean.DeviceType
import com.axend.radarcommandsdk.connect.bean.TcpMessageEntity
import com.axend.radarcommandsdk.connect.bean.TcpMsgTag
import com.axend.radarcommandsdk.connect.ble.BleClient
import com.axend.radarcommandsdk.connect.contract.IDeviceStatusCallback
import com.axend.radarcommandsdk.constant.STATUS_SUCCESS
import com.axend.radarcommandsdk.constant.STATUS_WIFI_NO_CONNECT
import com.axend.radarcommandsdk.utils.AppExecutors
import com.axend.radarcommandsdk.utils.LogUtil
import com.axend.radarcommandsdk.utils.ToastUtil
import com.axend.radarcommandsdk.utils.WifiUtils
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class DeviceNetworkConnect private constructor(
    private val deviceType: DeviceType?,
    private val iDeviceStatusCallback: IDeviceStatusCallback?,
    private val radarTcpServer: String?,
    private val wifiSSID: String?,
    private val wifiPassWord: String?
) {
    private lateinit var tcpConcurrentLinkedQueue: ConcurrentLinkedQueue<TcpMessageEntity>

    private lateinit var bleConcurrentLinkedQueue: ConcurrentLinkedQueue<BleMsgEntity>

    @Volatile
    private var mRun = true


    class Builder(private var context: Activity) {
        private var deviceType: DeviceType? = null
        private var iDeviceStatusCallback: IDeviceStatusCallback? = null
        private var radarTcpServer: String? = ""
        private var wifiSSID: String? = ""
        private var wifiPassWord: String? = "NONE"

        fun setDeviceType(deviceType: DeviceType?): Builder {
            this.deviceType = deviceType
            return this
        }

        fun setDeviceStatusCallback(iDeviceStatusCallback: IDeviceStatusCallback?): Builder {
            this.iDeviceStatusCallback = iDeviceStatusCallback
            return this
        }

        fun setRadarTcpServer(radarTcpServer: String?): Builder {
            this.radarTcpServer = radarTcpServer
            return this
        }

        fun setWifiSSID(wifiSSID: String?): Builder {
            this.wifiSSID = wifiSSID
            return this
        }

        fun setWifiPassWord(wifiPassWord: String?): Builder {
            this.wifiPassWord = wifiPassWord
            return this
        }

        fun build() = createInstance(
            context,
            deviceType,
            iDeviceStatusCallback,
            radarTcpServer,
            wifiSSID,
            wifiPassWord
        )

    }


    companion object {
        private const val TIME_OUT = 12 * 1000
        private const val BLE_SCAN_INTERVAL = 5000L

        private const val DELAY: Long = 1500

        var context: Activity? = null
        var applicationContext: Application? = null

        @Volatile
        private var mHasInit = false

        fun createInstance(
            context: Activity?,
            iDeviceType: DeviceType?,
            iDeviceStatusCallback: IDeviceStatusCallback?,
            radarTcpServer: String?,
            wifiSSID: String?,
            wifiPassWord: String?
        ): DeviceNetworkConnect {
            if (!mHasInit) {
                throw RuntimeException("must call DeviceNetworkConnect.init first")
            }

            context?.let {
                Companion.context = it
            }
            return DeviceNetworkConnect(
                iDeviceType,
                iDeviceStatusCallback,
                radarTcpServer,
                wifiSSID,
                wifiPassWord
            )
        }

        fun init(application: Application?) {
            application?.let {
                Companion.applicationContext = it
                mHasInit = true
            }
        }

    }


    init {
        deviceType?.let {
            it.getDeviceConnectInstance().setCallback(object : IDeviceStatusCallback {
                override fun callBackDeviceStatus(status: Int) {
                    if (status == STATUS_SUCCESS) {
                        sendMessageQueue()
                    }
                    launchUIThread {
                        iDeviceStatusCallback?.callBackDeviceStatus(status)
                    }
                }

                override fun callBackDeviceData(message: Any) {
                    launchUIThread {
                        iDeviceStatusCallback?.callBackDeviceData(message)
                    }


                    if (deviceType.deviceConnectType == DeviceConnectType.TYPE_BLE) {
                        if (TextUtils.isEmpty(message.toString())) {
                            if (linkedQueueIsEmpty()) {
                                stopSend()
                                close()
                            }
                        }
                        if (message is BleMsgEntity) {
                            when (message.bleMsgTag) {
                                BleMsgTag.GET_DEVICE_ID -> {
                                    executed()
                                    LogUtil.d("Wavve ID:${message.body.toString()}")
                                }

                                BleMsgTag.GET_ASSURE_DEVICE_ID -> {
                                    executed()
                                    LogUtil.d("Assure ID:${message.body.toString()}")
                                }

                                BleMsgTag.SET_STATUS -> {
                                    executed(message)
                                    LogUtil.d("set success:${message.body}")
                                }

                                else -> {
                                    if (linkedQueueIsEmpty()) {
                                        stopSend()
                                        close()
                                        LogUtil.d("commands send success")
                                    }
                                }
                            }
                        }
                    } else {
                        if (message is TcpMessageEntity) {
                            when (message.msgTag) {
                                TcpMsgTag.getID -> {
                                    val id: String = message.bodyObject.toString()
                                    LogUtil.d("radar id:$id")
                                    executed()
                                }

                                TcpMsgTag.getHardwareVersion -> {
                                    val hardwareVersion: String = message.bodyObject.toString()
                                    LogUtil.d("version:$hardwareVersion")
                                    executed()
                                }

                                TcpMsgTag.setFamilyWifiInfo ->                     
                                    if (message.bodyObject as Boolean) {
                                        LogUtil.d("setting wifi info success")
                                        executed()
                                    } else {
                                        LogUtil.d("setting wifi info fail")
                                    }

                                TcpMsgTag.setDeviceIPAndPort ->               
                                    if (message.bodyObject as Boolean) {
                                        LogUtil.d("setting ip and port success")
                                        executed()
                                    } else {
                                        LogUtil.d("setting ip and pord fail")
                                    }

                                else -> {}
                            }
                        }
                        if (linkedQueueIsEmpty()) {
                            stopSend()
                            close()
                            LogUtil.d("commads send complete")
                        }
                    }

                }

            })
            if (deviceType.deviceConnectType == DeviceConnectType.TYPE_WIFI) {
                initQueue(
                    TcpMessageEntity(
                        msgTag = TcpMsgTag.setFamilyWifiInfo,
                        bodyObject = "${wifiSSID},${wifiPassWord}" as Object
                    )
                )
            } else {
                initQueue(wifiSSID, wifiPassWord)
            }
        }
    }


    private inline fun initQueue(messageEntity: TcpMessageEntity) {
        mRun = true
        tcpConcurrentLinkedQueue = ConcurrentLinkedQueue<TcpMessageEntity>()
        tcpConcurrentLinkedQueue?.let {
            it.clear()
            it.offer(TcpMessageEntity(msgTag = TcpMsgTag.getID))
            it.offer(
                TcpMessageEntity(
                    msgTag = TcpMsgTag.setDeviceIPAndPort,
                    bodyObject = radarTcpServer as Object
                )
            )
            it.offer(TcpMessageEntity(msgTag = TcpMsgTag.getHardwareVersion))
            it.offer(messageEntity)
        }

    }

    private inline fun initQueue(ssid: String?, passWord: String?) {
        mRun = true
        bleConcurrentLinkedQueue = ConcurrentLinkedQueue<BleMsgEntity>()
        bleConcurrentLinkedQueue?.let {
            it.clear()
            it.offer(BleMsgEntity(bleMsgTag = BleMsgTag.RESET_DEVICE, radarType = deviceType))
            it.offer(
                BleMsgEntity(
                    bleMsgTag = if (deviceType === DeviceType.ASSURE) BleMsgTag.GET_ASSURE_DEVICE_ID else BleMsgTag.GET_DEVICE_ID,
                    radarType = deviceType
                )
            )
            it.offer(
                BleMsgEntity(
                    bleMsgTag = BleMsgTag.SET_NETWORK_INFO,
                    deviceType,
                    "$ssid,$passWord"
                )
            )
            it.offer(
                BleMsgEntity(
                    bleMsgTag = BleMsgTag.SET_SERVER_INFO,
                    deviceType,
                    radarTcpServer
                )
            )
            it.offer(BleMsgEntity(bleMsgTag = BleMsgTag.CLOSE_BLUETOOTH, deviceType, "cb,"))
        }
    }

    private inline fun executed() {
        if (deviceType?.deviceConnectType == DeviceConnectType.TYPE_WIFI && tcpConcurrentLinkedQueue != null) {
            tcpConcurrentLinkedQueue?.poll()
        } else {
            bleConcurrentLinkedQueue?.poll()
        }
    }

    fun executed(entity: BleMsgEntity) {
        if (deviceType?.deviceConnectType != DeviceConnectType.TYPE_WIFI) {
            if (null != bleConcurrentLinkedQueue && entity.bleMsgTag !== BleMsgTag.RESET_DEVICE) {
                bleConcurrentLinkedQueue.poll()
            }
        }
    }


    private inline fun sendMessageQueue() {
        AppExecutors.cpuIO.execute {
            when (deviceType?.deviceConnectType) {
                DeviceConnectType.TYPE_WIFI -> {
                    while (mRun && tcpConcurrentLinkedQueue?.isEmpty() == false) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(DELAY)
                            val tcpMessageEntity: TcpMessageEntity = tcpConcurrentLinkedQueue.peek()
                            tcpMessageEntity?.let {
                                deviceType.getDeviceConnectInstance().sendMsg(it)
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }


                DeviceConnectType.TYPE_BLE -> {
                    while (mRun && bleConcurrentLinkedQueue?.isEmpty() == false) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(DELAY)
                            val bleMessageEntity: BleMsgEntity = bleConcurrentLinkedQueue.peek()
                            if (null != bleMessageEntity) {
                                LogUtil.d("send：${bleMessageEntity.bleMsgTag}")
                                deviceType.getDeviceConnectInstance().sendMsg(bleMessageEntity)
                                //软重启命令只发一次，发完就移除
                                if (bleMessageEntity.bleMsgTag === BleMsgTag.RESET_DEVICE) {
                                    bleConcurrentLinkedQueue.poll()
                                } else if (bleMessageEntity.bleMsgTag === BleMsgTag.CLOSE_BLUETOOTH) {
                                    bleConcurrentLinkedQueue.poll()
                                }
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun deviceConnect() {
        when (deviceType?.deviceConnectType) {
            DeviceConnectType.TYPE_WIFI -> {
                if (WifiUtils.getInstance(context as Context).getWifiSSID().isNullOrEmpty()) {
                    launchUIThread {
                        iDeviceStatusCallback?.callBackDeviceStatus(STATUS_WIFI_NO_CONNECT)
                    }
                    return
                }
            }

            DeviceConnectType.TYPE_BLE -> {
                (deviceType.getDeviceConnectInstance() as BleClient).setDeviceName(deviceType.getDeviceNameByString())
            }

            else -> {}
        }

        deviceType?.getDeviceConnectInstance()?.connect()
    }


    fun linkedQueueIsEmpty(): Boolean {
        if (deviceType?.deviceConnectType == DeviceConnectType.TYPE_WIFI) {
            if (null != tcpConcurrentLinkedQueue) {
                return tcpConcurrentLinkedQueue.isEmpty()
            }
        } else {
            if (null != bleConcurrentLinkedQueue) {
                return bleConcurrentLinkedQueue.isEmpty()
            }
        }
        return true
    }


    fun stopSend() {
        mRun = false
    }


    private fun launchUIThread(runnable: Runnable?) = context?.runOnUiThread(runnable)

    fun close() {
        deviceType?.iDeviceConnect?.close()
    }


}