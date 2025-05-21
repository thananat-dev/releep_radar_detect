package com.axend.radarcommandsdk.connect.ble.handler

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.ble.AbstractBleMsgProcessor

import com.axend.radarcommandsdk.utils.LogUtil
import com.releep.releep_radar_detect.utils.ByteUtils


class BleAssureDeviceIdHandler : AbstractBleMsgProcessor() {


    override fun getMsgTag(): BleMsgTag {
        return BleMsgTag.GET_ASSURE_DEVICE_ID
    }

    override fun write(bleMsgEntity: BleMsgEntity): ByteArray {

        return byteArrayOf(
            0xAA.toByte(), 0xAA.toByte(), 0x55, 0x55,
            0x04, 0x00, 0x00, 0x02,
            0x00, 0x00, 0x00, 0x00
        )
    }

    override fun processor(bleMsgEntity: BleMsgEntity): BleMsgEntity? {
        if (null == bleMsgEntity) {
            LogUtil.d("notifyEntity is null")
        }

        bleMsgEntity?.let {
            val bytes = bleMsgEntity.body as ByteArray
            if (isNull(bytes)) {
                LogUtil.w("bytes length 0")
            }

            if (bytes.size != 13) {
                LogUtil.d("Device ID length is abnormal \"length: ${bytes.size}\"")
            }
            bytes?.let {
                val id = ByteUtils.bytesToHexString(it)
                return BleMsgEntity(getMsgTag(), id)

            }
        }


        return null
    }
}