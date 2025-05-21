package com.axend.radarcommandsdk.connect.contract

import com.axend.radarcommandsdk.connect.bean.BleMsgEntity

interface IBleMsgProcessor {

    fun write(bleMsgEntity: BleMsgEntity): ByteArray


    fun processor(bleMsgEntity: BleMsgEntity): BleMsgEntity?
}