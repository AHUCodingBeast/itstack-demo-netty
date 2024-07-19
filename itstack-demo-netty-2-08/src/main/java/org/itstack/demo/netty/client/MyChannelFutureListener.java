package org.itstack.demo.netty.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

/**
 * 这个监听器主要用于处理异步连接操作的结果，当一个客户端尝试与服务器建立连接时，它会等待ChannelFuture对象的通知来判断连接是否成功。
 */
public class MyChannelFutureListener implements ChannelFutureListener {
    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            System.out.println("itstack-demo-netty client start done. {关注公众号：bugstack虫洞栈，获取源码}");
            return;
        }
        /**
         * 如果连接失败，代码会从ChannelFuture对象中获取到事件循环组(EventLoop)实例，这是Netty用于执行I/O操作和任务调度的核心组件之一。
         * 使用loop.schedule方法安排一个在1秒后执行的任务。这通常用于重试或错误恢复场景。
         * 安排的任务是一个Runnable接口的匿名实现类，其中包含重新连接逻辑。这里创建一个新的NettyClient实例并尝试再次连接到指定地址和端口。
         * 连接成功后同样输出一条消息；如果在此过程中遇到异常，将打印出错误信息并提示进行重连。
         */
        final EventLoop loop = channelFuture.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    new NettyClient().connect("127.0.0.1", 7397);
                    System.out.println("itstack-demo-netty client start done. {关注公众号：bugstack虫洞栈，获取源码}");
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println("itstack-demo-netty client start error go reconnect ... {关注公众号：bugstack虫洞栈，获取源码}");
                }
            }
        }, 1L, TimeUnit.SECONDS);
    }
}
