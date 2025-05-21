package com.releep.releep_radar_detect

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.axend.radarcommandsdk.connect.DeviceNetworkConnect
import com.axend.radarcommandsdk.connect.bean.BleMsgEntity
import com.axend.radarcommandsdk.connect.bean.BleMsgTag
import com.axend.radarcommandsdk.connect.bean.DeviceType
import com.axend.radarcommandsdk.connect.bean.TcpMessageEntity
import com.axend.radarcommandsdk.connect.bean.TcpMsgTag
import com.axend.radarcommandsdk.connect.contract.IDeviceStatusCallback
import com.axend.radarcommandsdk.utils.LogUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** ReleepRadarDetectPlugin */
class ReleepRadarDetectPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context  // ประกาศตัวแปร context แบบกลาง
  private var activity: FlutterActivity? = null
//  private var activity: FlutterActivity? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
     context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "releep_radar_detect")
    channel.setMethodCallHandler(this)
    val application = flutterPluginBinding.applicationContext as Application  // ✅ cast เป็น Application
    DeviceNetworkConnect.init(application)  // ✅ ส่งให้ตรง
    LogUtil.d("ฟหกฟหกฟหก")


  }

  @RequiresApi(Build.VERSION_CODES.S)
  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "connect" -> {
        startConnectProcess(call, result)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  @RequiresApi(Build.VERSION_CODES.S)
  @SuppressLint("WrongConstant")
  private fun startConnectProcess(call: MethodCall, result: Result) {
    // ตรวจสอบว่า activity ถูกกำหนดหรือยัง
    if (activity == null) {
      result.error("NO_ACTIVITY", "Context is not an Activity", null)
      return
    }

    // ดึง argument มาจาก Flutter
    val wifiSsid = call.argument<String>("wifiSsid") ?: ""
    val wifiPassword = call.argument<String>("wifiPassword") ?: ""
    val radarTcpServiceUrl = call.argument<String>("radarTcpServiceUrl") ?: ""
    val deviceTypeInt = call.argument<Int>("deviceType") ?: 0
    val deviceType = DeviceType.WAAVE

    // เรียกใช้การขอ permission สำหรับ Bluetooth
    XXPermissions.with(activity!!.context)
      .permission(android.Manifest.permission.BLUETOOTH_CONNECT)
      .permission(android.Manifest.permission.BLUETOOTH_SCAN)
      .permission(android.Manifest.permission.ACCESS_FINE_LOCATION)
      .permission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
      .request(object : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
          if (!allGranted) {
            result.success("Permissions partially granted")
            return
          }

          // สร้างการเชื่อมต่อกับ DeviceNetworkConnect
          DeviceNetworkConnect.createInstance(
            context = activity as Activity,  // ใช้ activity ที่ไม่เป็น null
            iDeviceType = deviceType,  // เปลี่ยนค่า DeviceType
            radarTcpServer = radarTcpServiceUrl,
            wifiSSID = wifiSsid,
            wifiPassWord = wifiPassword,
            iDeviceStatusCallback = object : IDeviceStatusCallback {

              override fun callBackDeviceStatus(status: Int) {
                // การจัดการสถานะการเชื่อมต่อ
                LogUtil.i("status :$status")
                when (status) {
                  11 -> {
                    LogUtil.d("Device connection successful")
                  }
                  12 -> {
                    LogUtil.d("Device connection failed")
                  }
                  13 -> {
                    LogUtil.d("Device connection interruption")
                  }
                  else -> {}
                }
              }
              var hasSentResult = false

              override fun callBackDeviceData(message: Any) {
                // การจัดการข้อมูลที่ได้รับ
                if (message is BleMsgEntity) {
                  when (message.bleMsgTag) {
                    BleMsgTag.GET_DEVICE_ID -> {
                      LogUtil.d("Wavve ID:${message.body.toString()}")
                    }
                    BleMsgTag.GET_ASSURE_DEVICE_ID -> {
                      LogUtil.d("Assure ID:${message.body.toString()}")
                    }
                    BleMsgTag.SET_STATUS -> {
                      LogUtil.d("success:${message.body}")
                      if (!hasSentResult) {
                        hasSentResult = true
                        result.success(0)
                      }
                    }
                    else -> {}
                  }
                } else if (message is TcpMessageEntity) {
                  when (message.msgTag) {
                    TcpMsgTag.getID -> {
                      val id: String = message.bodyObject.toString()
                      LogUtil.d("radar id:$id")
                    }
                    TcpMsgTag.getHardwareVersion -> {
                      val hardwareVersion: String = message.bodyObject.toString()
                      LogUtil.d("version:$hardwareVersion")
                    }
                    TcpMsgTag.setFamilyWifiInfo -> {
                      if (message.bodyObject as Boolean) {
                        LogUtil.d("success")
                      }
                    }
                    TcpMsgTag.setDeviceIPAndPort -> {
                      if (message.bodyObject as Boolean) {
                        LogUtil.d("success")
                      }
                    }
                    else -> {}
                  }
                }
              }
            }
          ).deviceConnect()
        }

        override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
          result.error("PERMISSION_DENIED", "Permissions denied", null)
        }
      })
  }


  private fun getDeviceType(deviceTypeInt: Int): DeviceType {
    return when (deviceTypeInt) {
      0 -> DeviceType.WAAVE
      1 -> DeviceType.ASSURE
      else -> DeviceType.ASSURE_OLD  // ค่าเริ่มต้นเมื่อ deviceTypeInt ไม่ใช่ 0 หรือ 1
    }
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity as FlutterActivity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }
}
