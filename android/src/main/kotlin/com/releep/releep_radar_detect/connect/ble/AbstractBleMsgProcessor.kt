package com.axend.radarcommandsdk.connect.ble

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.contract.IBleMsgProcessor
import com.axend.radarcommandsdk.utils.LogUtil

abstract class AbstractBleMsgProcessor : IBleMsgProcessor {

    abstract fun getMsgTag(): BleMsgTag

    override fun write(bleMsgEntity: BleMsgEntity): ByteArray {
        if (null != bleMsgEntity) {
            val body: Any? = bleMsgEntity.body
            if (null != body) {
                if (body is String) {
                    return body.toString().toByteArray()
                } else if (body is ByteArray) {
                    return body
                }
            }
        }
        LogUtil.d(bleMsgEntity.toString())
        return ByteArray(0)
    }


    override fun processor(bleMsgEntity: BleMsgEntity): BleMsgEntity? {
        bleMsgEntity.bleMsgTag=getMsgTag()
        return bleMsgEntity
    }

    protected open fun isNull(data: ByteArray?): Boolean {
        return null == data || data.isEmpty()
    }
}