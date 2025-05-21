package com.axend.radarcommandsdk.connect.bean

enum class BleMsgTag(
    private val type: Int,
    private val command: Short,
    private val tagName: String
) {
    SET_STATUS(-1, 0x887,"setting result (success or fail)"),

    SET_SERVER_INFO(1, 0x889,"set sever socket address"),

    SET_NETWORK_INFO(2, 0x880,"set wifi info"),


    GET_DEVICE_ID(3, 0x0410, "get device's id"),
    RESET_DEVICE(4, 0x0411, "reboot"),
    GET_ASSURE_DEVICE_ID(5, 0x00, "get Assure ID"),

    CLOSE_BLUETOOTH(9, "close bluetooth"),
    ;


    constructor(type: Int, tagName: String) : this(type, 0, tagName)

    companion object {
        fun fromTag(tag: Short): BleMsgTag? {
            return BleMsgTag.values().firstOrNull { it.command == tag }
        }
    }


}