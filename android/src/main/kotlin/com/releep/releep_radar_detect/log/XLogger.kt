package com.axend.radarcommandsdk.log

import android.app.Application
import android.util.Log
//import com.tencent.mars.xlog.Log
//import com.tencent.mars.xlog.Xlog

class XLogger {

    fun init(context: Application, isDebug: Boolean, logPath: String, namePrefix: String = "sumTea") {
        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")

        val cachePath = context.filesDir.absolutePath + "sumTea/xlog"

//        val xlog = Xlog()
//        xlog.apply {
//            val level: Int
//            if (isDebug) {
//                level = Xlog.LEVEL_VERBOSE
//                xlog.setConsoleLogOpen(0, true)
//            } else {
//                level = Xlog.LEVEL_WARNING
//                xlog.setConsoleLogOpen(0, false)
//            }
//
//            appenderOpen(level, Xlog.AppednerModeAsync, cachePath, logPath, namePrefix, 0)
//        }
//        Log.setLogImp(xlog)
    }

    fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }


}