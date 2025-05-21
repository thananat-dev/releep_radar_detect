package com.axend.radarcommandsdk.connect.tcp

import com.axend.radarcommandsdk.connect.bean.BodyState
import com.axend.radarcommandsdk.connect.bean.TcpMessageEntity
import com.axend.radarcommandsdk.connect.bean.TcpMsgTag
import com.axend.radarcommandsdk.utils.LogUtil
import com.releep.releep_radar_detect.utils.ByteUtils
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import java.nio.ByteBuffer

class MessageCodec : MessageToMessageCodec<ByteBuf, TcpMessageEntity>() {

    override fun encode(ctx: ChannelHandlerContext?, msg: TcpMessageEntity?, out: MutableList<Any>?) {
        val bytes: ByteArray?
        when (msg?.msgTag) {
            TcpMsgTag.getID -> bytes = BodyState.getMessage.getCommandParameter()
            TcpMsgTag.getAlarmDelay -> bytes = BodyState.getMessage.getCommandParameter()
            TcpMsgTag.getDeviceHeight -> bytes = BodyState.getMessage.getCommandParameter()
            TcpMsgTag.getHardwareVersion -> bytes = BodyState.getMessage.getCommandParameter()
            TcpMsgTag.getWorkDistance -> bytes = BodyState.getMessage.getCommandParameter()
            TcpMsgTag.setDeviceIPAndPort -> bytes =
                msg.bodyObject.toString().toByteArray()
            TcpMsgTag.setFamilyWifiInfo -> bytes =
                msg.bodyObject.toString().toByteArray()

            else -> {
                bytes = msg?.body
            }
        }
        val buf = ctx!!.alloc().buffer()
        buf.writeBytes(TcpMessageEntity.messageHeader)
        buf.writeBytes(ByteUtils.short2ByteLE(bytes!!.size.toShort()))
        buf.writeBytes(msg?.msgTag?.getRadarCommandByte())
        buf.writeBytes(bytes)

        val b = ByteArray(buf.readableBytes())
        buf.markReaderIndex()
        buf.readBytes(b)
        buf.resetReaderIndex()
        LogUtil.d("send data:${ msg?.msgTag} ${ByteUtils.bytesToHexString(b)}" )

        out!!.add(buf)


    }

    override fun decode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: MutableList<Any>?) {
        val bytes = ByteArray(msg!!.readableBytes())
        msg.markReaderIndex()
        msg.readBytes(bytes)
        msg.resetReaderIndex()
        LogUtil.d("reciver data:" + ByteUtils.bytesToHexString(bytes))

        if (msg.readableBytes() < TcpMessageEntity.messageHeader.size
            || !getByteSize(msg, 4).contentEquals(TcpMessageEntity.messageHeader)
        ) {
            LogUtil.d("Message not head")
            return
        }
        val messageEntity = TcpMessageEntity()
        messageEntity.msgTag = TcpMsgTag.get(msg.readShortLE().toInt())
        msg.skipBytes(2)
        var bytesBody = getByteSize(msg, msg.readableBytes())
        messageEntity.length = bytesBody.size
        messageEntity.body = bytesBody
        messageEntity.bodyObject = getBodyObject(messageEntity.msgTag!!, bytesBody)

        out?.add(messageEntity)
    }


    private fun getByteSize(byteBuf: ByteBuf, size: Int): ByteArray {
        return byteBuf.readBytes(size).array()
    }


    fun getBodyObject(msgTag: TcpMsgTag, body: ByteArray): Object? {
        val result = when (msgTag) {
            TcpMsgTag.setFamilyWifiInfo -> BodyState.setSuccess.getCommandParameter()
                .contentEquals(body)

            TcpMsgTag.setAlarmDelay -> BodyState.setSuccess.getCommandParameter()
                .contentEquals(body)

            TcpMsgTag.setDeviceHeight -> BodyState.setSuccess.getCommandParameter()
                .contentEquals(body)

            TcpMsgTag.setWorkDistance -> BodyState.setSuccess.getCommandParameter()
                .contentEquals(body)

            TcpMsgTag.setDeviceIPAndPort -> BodyState.setSuccess.getCommandParameter()
                .contentEquals(body)

            TcpMsgTag.getID -> ByteUtils.bytesToHexString(body)
            TcpMsgTag.getAlarmDelay -> ByteUtils.byteToInt(body)
            TcpMsgTag.getWorkDistance -> ByteUtils.byteToFloat(body, 0)
            TcpMsgTag.getHardwareVersion -> getHardwareVersionByString(body)

            else -> {

            }
        }
        return result as Object?
    }


    private fun getHardwareVersionByString(bytes: ByteArray): String? {
        LogUtil.d(bytes.toString())
        LogUtil.d(ByteUtils.bytesToHexString(bytes))
        val byteBuffer = ByteBuffer.allocate(bytes.size)
        byteBuffer.put(bytes)
        byteBuffer.rewind()
        var str: String? = null
        for (i in 0..3) {
            val b: Byte = byteBuffer.get()
            val temp = ByteArray(4)
            temp[3] = b
            val hexString = java.lang.String.format("%02d", ByteUtils.byteToInt(temp))
            if (i == bytes.size - 1) {
                str += hexString
            } else {
                str += "$hexString."
            }
        }
        return str
    }


}