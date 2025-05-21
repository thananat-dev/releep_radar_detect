package com.axend.radarcommandsdk.utils

import android.os.Handler
import android.os.Looper
import android.os.Process
import java.lang.Integer.max
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object AppExecutors {

    private val threadFactory = ThreadFactory {
        Thread(it).apply {
            priority = Process.THREAD_PRIORITY_BACKGROUND
            setUncaughtExceptionHandler { t, e ->
                LogUtil.e(
                    "Thread<${t.name}> has uncaughtException",
                    e
                )
            }
        }
    }

    val cpuIO: Executor =
        CpuIOThreadExecutor(threadFactory)
    val diskIO: Executor =
        DiskIOThreadExecutor(threadFactory)
    val mainThread = MainThreadExecutor()

    class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }

        fun executeDelay(command: Runnable, delayMillis: Long) {
            mainThreadHandler.postDelayed(command, delayMillis)
        }
    }

    private class DiskIOThreadExecutor(threadFactory: ThreadFactory) : Executor {

        private val diskIO = Executors.newSingleThreadExecutor(threadFactory)

        override fun execute(command: Runnable) {
            val className = Throwable().stackTrace[1]?.className ?: "Undefined"
            val methodName = Throwable().stackTrace[1]?.methodName ?: "Undefined"
            diskIO.execute(RunnableWrapper("$className#$methodName", command))
        }
    }

    private class CpuIOThreadExecutor(threadFactory: ThreadFactory) : Executor {

        private val cpuIO = ThreadPoolExecutor(
            4,
            max(2, Runtime.getRuntime().availableProcessors()),
            1,
            TimeUnit.SECONDS,
            LinkedBlockingDeque<Runnable>(10),
            threadFactory,
            object : ThreadPoolExecutor.DiscardOldestPolicy() {
                override fun rejectedExecution(r: Runnable?, e: ThreadPoolExecutor?) {
                    super.rejectedExecution(r, e)
                    LogUtil.e("CpuIOThreadExecutor#rejectedExecution => Runnable <$r>")
                }
            }
        )

        override fun execute(command: Runnable) {
            val name = Throwable().stackTrace[1].className
            cpuIO.execute(RunnableWrapper(name, command))
        }
    }

}

private class RunnableWrapper(private val name: String, private val runnable: Runnable) : Runnable {
    override fun run() {
        Thread.currentThread().name = name
        runnable.run()
    }
}