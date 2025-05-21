package com.axend.radarcommandsdk.connect.ble.handler

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.bean.DeviceType
import com.axend.radarcommandsdk.connect.ble.AbstractBleMsgProcessor
import com.releep.releep_radar_detect.utils.ByteUtils

class BleResetDeviceHandler : AbstractBleMsgProcessor() {

    override fun getMsgTag(): BleMsgTag {
        return BleMsgTag.RESET_DEVICE
    }

    override fun write(bleMsgEntity: BleMsgEntity): ByteArray {
        val radarType: DeviceType? = bleMsgEntity.radarType
        val bytes: ByteArray =
            ByteUtils.hexStringToBytes("130101019B000000102700000006041100000000")

        if (radarType !== DeviceType.ASSURE) {
            bytes[0] = radarType!!.magicHead
        }
        return bytes
    }


    override fun processor(bleMsgEntity: BleMsgEntity): BleMsgEntity? {
        return super.processor(bleMsgEntity)
    }
}