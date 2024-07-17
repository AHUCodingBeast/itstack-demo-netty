package org.itstack.demo.netty.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * ChannelInboundHandler拦截和处理入站事件，
 * ChannelOutboundHandler拦截和处理出站事件。
 * ChannelHandler和ChannelHandlerContext通过组合或继承的方式关联到一起成对使用。
 * 事件通过ChannelHandlerContext主动调用如read(msg)、write(msg)和fireXXX()等方法，将事件传播到下一个处理器。
 * 注意：入站事件在ChannelPipeline双向链表中由头到尾正向传播，出站事件则方向相反。
 *
 *
 * 当客户端连接到服务器时，Netty新建一个ChannelPipeline处理其中的事件，而一个ChannelPipeline中含有若干ChannelHandler。
 * 如果每个客户端连接都新建一个ChannelHandler实例，当有大量客户端时，服务器将保存大量的ChannelHandler实例。
 * 为此，Netty提供了Sharable注解，如果一个ChannelHandler状态无关，那么可将其标注为Sharable，如此，服务器只需保存一个实例就能处理所有客户端的事件。
 */
@ChannelHandler.Sharable
public class MyOutMsgHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("ChannelOutboundHandlerAdapter.read 发来一条消息\r\n");
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.writeAndFlush("ChannelOutboundHandlerAdapter.write 发来一条消息\r\n");
        super.write(ctx, msg, promise);
    }

}
