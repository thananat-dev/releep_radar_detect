package com.axend.radarcommandsdk.connect.ble.handler

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.ble.AbstractBleMsgProcessor

class BleDeviceServerHandler:AbstractBleMsgProcessor() {

    override fun getMsgTag(): BleMsgTag {
        return BleMsgTag.SET_SERVER_INFO
    }

    override fun write(bleMsgEntity: BleMsgEntity): ByteArray {
        val body: Any? = bleMsgEntity.body
        if (body is String) {
            val data = "[$body]"
            bleMsgEntity.body=data
        }
        return super.write(bleMsgEntity)
    }
}