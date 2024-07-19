package org.itstack.demo.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.itstack.demo.netty.util.SerializationUtil;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛关注获取学习源码｝
 * 虫洞群：①群5398358 ②群5360692
 * Create by fuzhengwei on 2019
 */
public class ObjEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public ObjEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    /**
     * 代码解释：
     * 首先检查输入对象in是否是genericClass类型的实例。
     * 如果是，则使用SerializationUtil.serialize()方法将对象序列化为字节数组data。
     * 接下来，在输出缓冲区out中写入data的长度，这有助于解码端知道如何正确读取后续的数据。
     * 最后，将整个字节数组data写入到out中。
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out)  {
        if (genericClass.isInstance(in)) {
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

}
