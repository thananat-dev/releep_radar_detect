package com.axend.radarcommandsdk.connect.ble.handler

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.ble.AbstractBleMsgProcessor
import com.axend.radarcommandsdk.utils.LogUtil

class BleSetStateHandler : AbstractBleMsgProcessor() {

    companion object {
        const val SET_SUCCESS: Byte = 0x01
    }


    override fun getMsgTag(): BleMsgTag {
        return BleMsgTag.SET_STATUS
    }

    override fun write(bleMsgEntity: BleMsgEntity): ByteArray {
        return ByteArray(0)
    }


    override fun processor(bleMsgEntity: BleMsgEntity): BleMsgEntity? {
        if (null == bleMsgEntity) {
            LogUtil.w("notifyEntity is null")
            return null
        }

        val bytes = bleMsgEntity.body as ByteArray

        if (isNull(bytes)) {
            LogUtil.w("bytes length 0")
            return null
        }

        val status = bytes[0] == SET_SUCCESS
        return BleMsgEntity(getMsgTag(), status)
    }
}