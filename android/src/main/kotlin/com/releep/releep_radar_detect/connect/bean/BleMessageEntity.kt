package com.axend.radarcommandsdk.connect.bean

//import com.axend.radarcommandsdk.utils.ByteUtils
import com.releep.releep_radar_detect.utils.ByteUtils
import java.nio.charset.StandardCharsets

data class BleMessageEntity(
    val tag: Tag,
    val message:String,

){


    fun getMessage(): ByteArray? {
        if (tag == Tag.serverInfo) {
            return "[$message]".toByteArray(StandardCharsets.UTF_8)
        } else if (tag == Tag.networkInfo) {
            return "($message)".toByteArray(StandardCharsets.UTF_8)
        } else if (tag == Tag.getDeviceUUID) {
            return ByteUtils.hexStringToBytes("130101019B000000102700000006041000000000")
        } else if (tag == Tag.resetDevice) {
            return ByteUtils.hexStringToBytes("130101019B000000102700000006041100000000")
        }
        return null
    }

    enum class Tag {
        serverInfo, networkInfo, getDeviceUUID, resetDevice
    }

}
