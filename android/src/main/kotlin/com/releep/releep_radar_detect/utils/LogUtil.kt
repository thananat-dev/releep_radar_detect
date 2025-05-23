package com.axend.radarcommandsdk.utils

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.axend.radarcommandsdk.log.XLogger

/**
 *  API is the same to {@link android.util.Log}
 */
object LogUtil {
    private const val TAG = "LogUtil"
    var application: Application? = null

    private var isDebug = false

    private var logger: XLogger = XLogger()

    private var mLogPath: String? = null

    fun init(application: Application, logPath: String, namePrefix: String = "SRM", isDebug: Boolean = false) {
        LogUtil.application = application
        LogUtil.isDebug = isDebug
        mLogPath = logPath
//        logger.init(application, isDebug, logPath, namePrefix)
        // persist env info
        val metrics = DisplayMetrics()
        (application.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getMetrics(metrics)
        val envInfo = """
            *******************************
            ${DeviceInfoUtils.phoneManufacturer}_${DeviceInfoUtils.phoneModel}_${metrics.widthPixels}x${metrics.heightPixels}_${metrics.densityDpi}_${Build.VERSION.SDK_INT}
            *******************************
        """.trimIndent()
//        logger.e(TAG, envInfo)
    }

    @JvmOverloads
    fun v(message: String, throwable: Throwable? = null, tag: String? = null, saveLog: Boolean = false) {
        prepareLog(Log.VERBOSE, tag, message, throwable, saveLog)
    }

    @JvmOverloads
    fun d(message: String, throwable: Throwable? = null, tag: String? = null, saveLog: Boolean = false) {
        prepareLog(Log.DEBUG, tag, message, throwable, saveLog)
    }

    @JvmOverloads
    fun i(message: String, throwable: Throwable? = null, tag: String? = null, saveLog: Boolean = false) {
        prepareLog(Log.INFO, tag, message, throwable, saveLog)
    }

    @JvmOverloads
    fun w(message: String, throwable: Throwable? = null, tag: String? = null, saveLog: Boolean = false) {
        prepareLog(Log.WARN, tag, message, throwable, saveLog)
    }

    @JvmOverloads
    fun e(message: String, throwable: Throwable? = null, tag: String? = null, saveLog: Boolean = false) {
        prepareLog(Log.ERROR, tag, message, throwable, saveLog)
    }

    @JvmOverloads
    fun w(throwable: Throwable? = null, saveLog: Boolean = false) {
        prepareLog(Log.WARN, "", "", throwable, saveLog)
    }

    @JvmOverloads
    fun e(throwable: Throwable? = null, saveLog: Boolean = false) {
        prepareLog(Log.ERROR, "", "", throwable, saveLog)
    }

    private fun prepareLog(priority: Int, tag: String?, message: String, throwable: Throwable?, saveLog: Boolean) {
        val logTag = tag ?: TAG
        throwable?.let {
        } ?: run {
            when (priority) {
                Log.VERBOSE -> Log.v(logTag, message)
                Log.DEBUG -> Log.d(logTag, message)
                Log.INFO -> Log.i(logTag, message)
                Log.WARN -> Log.w(logTag, message)
                Log.ERROR -> Log.e(logTag, message)
                else -> Log.v(logTag, message)
            }
        }
    }

    fun flushLog() {
//        logger.flushLog()
    }

    fun getLogPath(): String? {
        return mLogPath
    }

}
