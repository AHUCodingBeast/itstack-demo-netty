package org.itstack.demo.netty.server;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.ReferenceCountUtil;

/**
 * 重写了父类ChannelOutboundHandlerAdapter的write方法，这是当有数据需要通过通道发送时调用的方法。主要逻辑如下：
 * 类型检查：确认传入的消息msg是否为ByteBuf实例。如果不是，则直接调用父类的write方法进行默认处理。
 * <p>
 * 数据提取：如果msg是ByteBuf，则将其转换为字节数组data。这里使用了getData私有方法来完成这一操作。
 * 创建输入流：将字节数组封装到ByteInputStream对象in中，以便后续作为数据来源。
 * 创建分块流：基于ByteInputStream创建ChunkedStream对象stream，设置每次读取的数据量为10字节。这在实际应用中可以根据网络状况或需求调整大小。
 * 创建进度承诺：生成ChannelProgressivePromise对象progressivePromise，用于跟踪数据发送过程中的进度信息。
 * 监听器添加：给progressivePromise添加一个ChannelProgressiveFutureListener监听器，用于在数据发送完成后执行相应的回调函数。这个监听器有两个方法：
 * operationProgressed：在数据发送过程中被调用，但在这个实现中没有具体逻辑。
 * operationComplete：在数据发送完毕后被调用，根据发送结果打印日志信息，并更新原始的ChannelPromise状态。
 * 释放资源：通过ReferenceCountUtil.release(msg)确保ByteBuf资源得到正确释放，避免内存泄漏。
 * <p>
 * 数据写入：最后，使用ctx.write(stream, progressivePromise)将分块流写入通道，同时关联进度承诺以监控写入过程。
 */
public class MyServerChunkHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //内容验证
        if (!(msg instanceof ByteBuf)) {
            super.write(ctx, msg, promise);
            return;
        }
        //获取Byte
        ByteBuf buf = (ByteBuf) msg;
        byte[] data = this.getData(buf);
        //写入流中
        ByteInputStream in = new ByteInputStream();
        in.setBuf(data);
        //消息分块；10个字节，测试过程中可以调整
        ChunkedStream stream = new ChunkedStream(in, 10);
        //管道消息传输承诺
        ChannelProgressivePromise progressivePromise = ctx.channel().newProgressivePromise();
        progressivePromise.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("消息发送成功 success");
                    promise.setSuccess();
                } else {
                    System.out.println("消息发送失败 failure：" + future.cause());
                    promise.setFailure(future.cause());
                }
            }
        });
        ReferenceCountUtil.release(msg);
        ctx.write(stream, progressivePromise);
    }

    //获取Byte
    private byte[] getData(ByteBuf buf) {
        if (buf.hasArray()) {
            return buf.array().clone();
        }
        byte[] data = new byte[buf.readableBytes() - 1];
        buf.readBytes(data);
        return data;
    }

}
