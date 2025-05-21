package com.axend.radarcommandsdk.connect.ble.handler

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.bean.DeviceType
import com.axend.radarcommandsdk.connect.ble.AbstractBleMsgProcessor
import com.axend.radarcommandsdk.utils.LogUtil
import com.releep.releep_radar_detect.utils.ByteUtils


class BleDeviceIdHandler : AbstractBleMsgProcessor() {

    override fun getMsgTag(): BleMsgTag {
        return BleMsgTag.GET_DEVICE_ID
    }


    override fun write(bleMsgEntity: BleMsgEntity): ByteArray {
        val bytes: ByteArray =
            ByteUtils.hexStringToBytes("130101019B000000102700000006041000000000")
        val radarType: DeviceType? = bleMsgEntity.radarType
        //Assure雷达不走蓝牙协议
        if (radarType !== DeviceType.ASSURE) {
            bytes[0] = radarType!!.magicHead
        }

        return bytes
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
                LogUtil.d("Device ID length is abnormal \"length: ${bytes.size}\""  )
            }
            bytes?.let {
                val id = ByteUtils.bytesToHexString(it)
                return BleMsgEntity(getMsgTag(), id)

            }
        }


        return null


    }
}