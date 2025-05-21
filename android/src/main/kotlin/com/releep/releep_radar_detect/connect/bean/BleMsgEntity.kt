package com.axend.radarcommandsdk.connect.bean

data class BleMsgEntity constructor(
    var bleMsgTag: BleMsgTag?,
    val radarType: DeviceType?,
    var body: Any?
) {
    constructor(bleMsgTag: BleMsgTag, body: Any?) : this(bleMsgTag, null, body)
    constructor(bleMsgTag: BleMsgTag, radarType: DeviceType?) : this(bleMsgTag, radarType, null)

}
