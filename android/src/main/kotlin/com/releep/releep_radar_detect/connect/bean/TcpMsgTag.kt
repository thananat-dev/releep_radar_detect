package com.axend.radarcommandsdk.connect.bean

import com.axend.radarcommandsdk.utils.LogUtil

enum class TcpMsgTag(private val tag: Int, private val bytes: ByteArray) {
    getID(2, byteArrayOf(0x00, 0x02)),   
    setWorkDistance(3, byteArrayOf(0x00, 0x03)),  
    getWorkDistance(4, byteArrayOf(0x00, 0x04)),   
    setAlarmDelay(5, byteArrayOf(0x00, 0x05)),   
    getAlarmDelay(6, byteArrayOf(0x00, 0x06)),   
    switchSTAMode(7, byteArrayOf(0x00, 0x07)),   
    setFamilyWifiInfo(10, byteArrayOf(0x00, 0x0A)),   
    getHardwareVersion(11, byteArrayOf(0x00, 0x0B)),   
    setDeviceIPAndPort(12, byteArrayOf(0x00, 0x0C)),   
    setDeviceHeight(25, byteArrayOf(0x00, 0x19)),   
    getDeviceHeight(26, byteArrayOf(0x00, 0x1A)), ; 
    companion object{
         private var map: Map<Int, TcpMsgTag>? = mutableMapOf()

        fun get(tag: Int): TcpMsgTag?{
            for (msg in TcpMsgTag.values()) {
                (map as MutableMap<Int, TcpMsgTag>)[msg.getRadarCommandTag()] = msg
            }
//            LogUtil.d("${map}")
//            LogUtil.d("${map?.get(2)}")
            return map?.get(tag)
        }

    }


    fun getRadarCommandByte(): ByteArray {
        return bytes
    }

    fun getRadarCommandTag(): Int {
        return tag
    }


}