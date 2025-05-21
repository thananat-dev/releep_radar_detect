package com.axend.radarcommandsdk.connect.contract

interface IDeviceStatusCallback {

    fun callBackDeviceStatus(status: Int)

    fun callBackDeviceData(message:Any)

}