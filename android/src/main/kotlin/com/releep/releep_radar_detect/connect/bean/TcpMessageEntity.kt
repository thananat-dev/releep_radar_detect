package com.axend.radarcommandsdk.connect.bean

data class TcpMessageEntity(
    var msgTag: TcpMsgTag? = null,
    var length: Int? = null,
    var body: ByteArray? = null,
    var bodyObject: Object? = null
) {
    companion object {
        val messageHeader: ByteArray = byteArrayOf(0xAA.toByte(), 0xAA.toByte(), 0x55, 0x55)
    }


}
