package com.axend.radarcommandsdk.connect.ble

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.bean.DeviceType
//import com.axend.radarcommandsdk.utils.ByteUtils
import com.axend.radarcommandsdk.utils.LogUtil
import com.releep.releep_radar_detect.utils.ByteUtils
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class BleHandlerManager {

    private var bleMsgHandlerMap: ConcurrentHashMap<BleMsgTag, AbstractBleMsgProcessor> =
        ConcurrentHashMap()


    fun addHandler(vararg bleMsgCodecs: AbstractBleMsgProcessor?) {
        if (null == bleMsgCodecs) {
            LogUtil.w("bleMsgCodecs is null")
            return
        }
        for (msgCodec in bleMsgCodecs) {
            addHandler(msgCodec)
        }
    }

    fun addHandler(bleMsgCodec: AbstractBleMsgProcessor?) {
        if (null == bleMsgCodec) {
            LogUtil.w("bleMsgCodec is null")
            return
        }
        bleMsgHandlerMap[bleMsgCodec.getMsgTag()] = bleMsgCodec
    }


    fun getHandler(bleMsgTag: BleMsgTag?): AbstractBleMsgProcessor? {
        return if (null == bleMsgTag) {
            null
        } else bleMsgHandlerMap[bleMsgTag]
    }

    fun removeHandler(bleMsgTag: BleMsgTag?) {
        if (null == bleMsgTag) {
            LogUtil.w("bleMsgTag is null")
            return
        }
        bleMsgHandlerMap.remove(bleMsgTag)
    }

    fun release() {
        if (null == bleMsgHandlerMap || bleMsgHandlerMap.size == 0) {
            return
        }
        bleMsgHandlerMap.clear()
    }

    fun getWriteBleMsg(bleMsgEntity: BleMsgEntity?): ByteArray? {
        if (null == bleMsgEntity) {
            LogUtil.w("bleMsgEntity is null")
            return ByteArray(0)
        }
        val handler = getHandler(bleMsgEntity.bleMsgTag)
        return handler!!.write(bleMsgEntity)
    }


    fun decode(data: ByteArray?): BleMsgEntity? {
        if (null == data || data.size == 0) {
            LogUtil.w("data is null $data")
            return null
        }
        val dataLength = data.size
        return if (dataLength > 1) {
            //radar data handler
            findProcessorHandler(data)
        } else {
            //mcu data handler
            val handler = getHandler(BleMsgTag.SET_STATUS)
            if (null == handler) {
                LogUtil.w("not found handler")
                return null
            }
            val notifyEntity = BleMsgEntity(BleMsgTag.SET_STATUS, data)
            handler.processor(notifyEntity)
        }
    }


    private fun findProcessorHandler(data: ByteArray): BleMsgEntity? {
        val buffer = ByteBuffer.wrap(data)
        val magic = buffer.get()
        val radarType: DeviceType? = DeviceType.getMagic(magic)
        if (radarType == null) {
            LogUtil.d("magic undefined $data")
        }
        val position = if (radarType == null) 6 else 14
        buffer.position(position)
        val tag = buffer.short
        val bleMsgTag = BleMsgTag.fromTag(tag)
        LogUtil.d(tag="tran", message = "tag: $tag $bleMsgTag hex format ${ByteUtils.bytesToHexString(data)}")
        val handler = getHandler(bleMsgTag)
        if (null == handler) {
            LogUtil.w("not found handler")
            return null
        }

        val size = buffer.limit() - buffer.position()
        if (size <= 0) {
            LogUtil.w("buffer readable content length is 0")
            return null
        }
        val body = ByteArray(size)
        buffer[body]
        val notifyEntity = BleMsgEntity(bleMsgTag, radarType, body)
        buffer.clear()
        return handler.processor(notifyEntity)
    }

}