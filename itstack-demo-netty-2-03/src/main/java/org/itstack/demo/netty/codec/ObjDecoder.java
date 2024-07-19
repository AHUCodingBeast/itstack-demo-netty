package org.itstack.demo.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.itstack.demo.netty.util.SerializationUtil;

import java.util.List;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛关注获取学习源码｝
 * 虫洞群：①群5398358 ②群5360692
 * Create by fuzhengwei on 2019
 */
public class ObjDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public ObjDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    /**
     * 首先检查输入缓冲区（in）是否至少有4个可读字节，如果没有，则直接返回。
     * 然后标记当前读取位置，以便如果数据不足时可以回退到当前位置。
     * 接下来读取前四个字节作为数据长度。
     * 如果剩余的可读字节数小于之前读取的数据长度，说明数据不完整，此时需要重置读取位置并返回，等待更多数据到来。
     * 创建一个与数据长度相等的字节数组，并从输入缓冲区读取相应数量的字节到数组中。
     * 最后使用SerializationUtil.deserialize方法将字节数组反序列化为目标类型对象，并添加到输出列表out中。
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        out.add(SerializationUtil.deserialize(data, genericClass));
    }

}
