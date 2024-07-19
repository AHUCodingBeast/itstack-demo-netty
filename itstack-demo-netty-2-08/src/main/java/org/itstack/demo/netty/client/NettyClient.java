package org.itstack.demo.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛获取学习源码｝
 * Create by fuzhengwei on 2019
 */
public class NettyClient {

    public static void main(String[] args) {
        new NettyClient().connect("127.0.0.1", 7397);
    }

    public void connect(String inetHost, int inetPort) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.AUTO_READ, true);
            b.handler(new MyChannelInitializer());

            //尝试与给定的主机和端口建立连接，并等待直到连接成功或失败。
            // 如果连接成功，返回的ChannelFuture对象可以用来检查连接状态或注册回调函数。
            ChannelFuture f = b.connect(inetHost, inetPort).sync();

            //添加监听，处理重连
            f.addListener(new MyChannelFutureListener());

            //等待通道关闭，确保所有相关的资源都被正确释放。
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅地关闭了线程组，确保所有正在运行的任务完成后再停止线程。
            workerGroup.shutdownGracefully();
        }
    }

}
