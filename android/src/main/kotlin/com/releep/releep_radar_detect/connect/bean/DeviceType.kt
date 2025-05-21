package com.axend.radarcommandsdk.connect.bean

//import com.axend.radarcommandsdk.R
//import com.axend.radarcommandsdk.connect.DeviceNetworkConnect.Companion.applicationContext
import com.axend.radarcommandsdk.connect.ble.BleClient
import com.axend.radarcommandsdk.connect.contract.IDeviceConnect
import com.axend.radarcommandsdk.connect.tcp.NettyClient



enum class DeviceType(
    private val deviceType: Int,
    val deviceName: String,
    val deviceConnectType: DeviceConnectType,
    val iDeviceConnect: IDeviceConnect,
    val magicHead: Byte
) {

    ASSURE(
        0,
        "Aerosense Wavve",
        DeviceConnectType.TYPE_BLE,
        BleClient.instance,
        0x00
    ),

    WAAVE(
        1,
        "Aerosense Wavve",
        DeviceConnectType.TYPE_BLE, BleClient.instance,
        0x13),

    ASSURE_OLD(
        4,
        "Aerosense Wavve",
        DeviceConnectType.TYPE_WIFI,
        NettyClient.instance,
        0x00
    );


    companion object {
        var typeMap: HashMap<Int, DeviceType?> = hashMapOf()
        var nameMap: HashMap<String?, DeviceType?> = hashMapOf()
        var magicMap: HashMap<Byte, DeviceType?> = hashMapOf()

        init {
            for (r in DeviceType.values()) {
                typeMap[r.getType()] = r
                nameMap[r.getDeviceNameByString()] = r
                magicMap[r.getMagic()] = r
            }
        }

        fun getMagic(magicHead: Byte): DeviceType? {
            return magicMap[magicHead]
        }
    }

    fun getDeviceConnectInstance(): IDeviceConnect {
        return iDeviceConnect
    }


    private fun getType(): Int {
        return deviceType
    }


    fun getDeviceNameByString(): String? {
        return deviceName
    }

    private fun getMagic(): Byte {
        return magicHead
    }

    open fun getMagicForMap(magicHead: Byte): DeviceType? {
        return magicMap[magicHead]
    }


}