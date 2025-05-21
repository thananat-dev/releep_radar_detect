package com.axend.radarcommandsdk.connect.tcp

import com.axend.radarcommandsdk.connect.bean.DeviceConnectType
import com.axend.radarcommandsdk.connect.bean.TcpMessageEntity
import com.axend.radarcommandsdk.connect.contract.IDeviceConnect
import com.axend.radarcommandsdk.connect.contract.IDeviceStatusCallback
import com.axend.radarcommandsdk.constant.STATUS_BROKEN
import com.axend.radarcommandsdk.constant.STATUS_FAILED
import com.axend.radarcommandsdk.constant.STATUS_SUCCESS
import com.axend.radarcommandsdk.utils.AppExecutors
import com.axend.radarcommandsdk.utils.LogUtil
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import java.util.concurrent.TimeUnit


class NettyClient private constructor() : IDeviceConnect {

    private val WIFI_SERVICE_IP = "10.10.100.254"
    private val WIFI_SERVICE_PORT = 8899
    private val WIFI_RECONNECT_COUNT = 3

    private var eventLooperGroup: NioEventLoopGroup = NioEventLoopGroup(9)
    private lateinit var bootstrap: Bootstrap
    private var parentChannelFuture: ChannelFuture? = null
    private var channel: Channel? = null
    private lateinit var channelHandlerContext: ChannelHandlerContext
    private var nioSocketChannel: NioSocketChannel? = null
    private lateinit var mCallback: IDeviceStatusCallback


    private var retryCount = WIFI_RECONNECT_COUNT

    @Volatile
    private var isClose = false

    companion object {
        val instance = NettyClientInstance.holder
    }

    private object NettyClientInstance {
        val holder = NettyClient()
    }

    init {
        init()
    }

    private fun init() {
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
        bootstrap = Bootstrap().group(eventLooperGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .handler(object : ChannelInitializer<NioSocketChannel>() {

                override fun initChannel(ch: NioSocketChannel?) {
                    ch!!.pipeline()
                        .addLast(MessageCodec()) 
                        .addLast(object : ChannelInboundHandlerAdapter() {

                            override fun channelActive(ctx: ChannelHandlerContext?) {
                                channel = ctx!!.channel()
                                channelHandlerContext = ctx
                                nioSocketChannel = ch
                                LogUtil.d(ctx.channel().toString() + "  connected")
//                                         ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(ByteUtils.hexStringToBytes("AAAA55550400000200000000")));
                                //                                         ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(ByteUtils.hexStringToBytes("AAAA55550400000200000000")));
                                callbackState(STATUS_SUCCESS)
                            }

                            override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
                                val message: TcpMessageEntity? =
                                    if (msg is TcpMessageEntity) msg as TcpMessageEntity? else null
                                LogUtil.d("read:" + msg.toString())
                                mCallback.callBackDeviceData(message as Object)
                            }

                            override fun channelInactive(ctx: ChannelHandlerContext?) {
                                if (isClose) {
                                    LogUtil.d(ctx!!.channel().toString() + " connect isClose")
                                } else {
                                    callbackState(STATUS_BROKEN)
                                    LogUtil.d(ctx!!.channel().toString() + " connect is broken")
                                }
                            }

                            override fun channelRegistered(ctx: ChannelHandlerContext?) {
                                LogUtil.d("connect fail")
                            }

                            override fun exceptionCaught(
                                ctx: ChannelHandlerContext?,
                                cause: Throwable?
                            ) {
                                cause!!.printStackTrace()
                                ctx!!.close()
                            }
                        })
                }

            })
    }


    override fun connect() {
        if (bootstrap == null || parentChannelFuture == null || isConnect()) {
            init()
        } else {
            if (parentChannelFuture != null && parentChannelFuture!!.channel() != null) {
                parentChannelFuture!!.cancel(true)
                println("channelFuture------>")
                eventLooperGroup.shutdownGracefully()
                init()
            }
        }

        isClose = false

        AppExecutors.cpuIO.execute {
            try {
                parentChannelFuture =
                    bootstrap?.connect(WIFI_SERVICE_IP, WIFI_SERVICE_PORT)
                        ?.sync()
            } catch (e: Exception) {
                e.printStackTrace()
                reConnect()
            }
        }
    }

    private fun reConnect() {
        AppExecutors.cpuIO.execute {
            do {
                try {
                    TimeUnit.SECONDS.sleep(3)
                    parentChannelFuture = if (parentChannelFuture == null) {
                        bootstrap.connect(WIFI_SERVICE_IP, WIFI_SERVICE_PORT)
                            .sync()
                    } else {
                        break
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } while (retryCount-- > 0)

            if (!isConnect()) {
                callbackState(STATUS_FAILED)
            }
        }
    }

    override fun setCallback(statusCallback: IDeviceStatusCallback?) {
        if (statusCallback != null) {
            mCallback = statusCallback
        }
    }

    override fun isConnect(): Boolean {
        return channel != null && channel!!.isActive
    }

    override fun close() {
        isClose = true
        AppExecutors.cpuIO.execute {
            if (parentChannelFuture != null && eventLooperGroup != null) {
                parentChannelFuture!!.channel().close()
                eventLooperGroup.shutdownGracefully()
            }
        }
    }

    override fun sendMsg(obj: Any?) {
        if (channel == null || channelHandlerContext == null || obj == null ||
            obj !is TcpMessageEntity
        ) return

        AppExecutors.cpuIO.execute {
            channel!!.writeAndFlush(obj)
        }
    }

    override fun getConnectType(): DeviceConnectType? {
        TODO("Not yet implemented")
    }


    private fun callbackState(state: Int) {
        if (null != mCallback) {
            mCallback.callBackDeviceStatus(state)
        } else {
            LogUtil.d("not set callback！")
        }
    }
}