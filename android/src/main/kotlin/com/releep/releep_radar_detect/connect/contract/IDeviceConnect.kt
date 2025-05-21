package com.axend.radarcommandsdk.connect.contract

import com.axend.radarcommandsdk.connect.bean.DeviceConnectType

interface IDeviceConnect {

    fun connect();

    fun setCallback(statusCallback: IDeviceStatusCallback?)

    fun isConnect(): Boolean

    fun close()

    fun sendMsg(obj: Any?)

    /** get connect type :wifi or net*/
    fun getConnectType(): DeviceConnectType?

}