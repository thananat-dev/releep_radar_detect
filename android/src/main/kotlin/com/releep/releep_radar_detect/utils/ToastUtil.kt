package com.axend.radarcommandsdk.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtil {

    fun showShort(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun showLong(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_LONG).show()
    }

    fun showShortInCenter(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    fun showLongInCenter(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_LONG).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }
}