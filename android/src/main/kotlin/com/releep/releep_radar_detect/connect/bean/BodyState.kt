package com.axend.radarcommandsdk.connect.bean



enum class BodyState(val state: Int, private val bytes: ByteArray) {
    setSuccess(1, byteArrayOf(0x01, 0x00, 0x00, 0x00)),  
    setFailed(2, byteArrayOf(0x00, 0x00, 0x00, 0x00)),  
    getMessage(3, byteArrayOf(0x00, 0x00, 0x00, 0x00))  ;


    fun getCommandParameter(): ByteArray = bytes
}