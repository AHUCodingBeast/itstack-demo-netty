package org.itstack.demo.netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.charset.Charset;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛获取学习源码｝
 * Create by fuzhengwei on 2019
 */
public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) {
        /**
         * 心跳监测
         * 1、readerIdleTimeSeconds 读超时时间
         * 2、writerIdleTimeSeconds 写超时时间
         * 3、allIdleTimeSeconds    读写超时时间
         * 4、TimeUnit.SECONDS 秒[默认为秒，可以指定]
         * 下面这行代码的意思是：如果在2秒内没有读、写或读写操作，就会触发对应的空闲事件。
         * 在Netty框架中，IdleStateHandler是一个重要的处理器，主要用于检测通道两端长时间没有读、写或者读写操作的情况，即所谓的“空闲”状态。
         * 它的核心作用在于帮助实现长连接的心跳机制，这对于保持网络连接的健康状态至关重要，尤其是对于那些需要维持持久化连接的应用场景来说。
         * IdleStateHandler 在检测到空闲事件的时候 会触发一个IdleStateEvent事件，这个事件会被传递给管道中的下一个处理器（代码中在MyServerHandler进行了处理）。
         */
        channel.pipeline().addLast(new IdleStateHandler(2, 2, 2));
        // 基于换行符号
        channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
        // 解码转String，注意调整自己的编码格式GBK、UTF-8
        channel.pipeline().addLast(new StringDecoder(Charset.forName("GBK")));
        // 解码转String，注意调整自己的编码格式GBK、UTF-8
        channel.pipeline().addLast(new StringEncoder(Charset.forName("GBK")));
        // 在管道中添加我们自己的接收数据实现方法
        channel.pipeline().addLast(new MyServerHandler());
    }

}
