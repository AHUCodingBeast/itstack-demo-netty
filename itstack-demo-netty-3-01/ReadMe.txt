ByteBuf 和数据发送的关系
在 Netty 中，ByteBuf 与数据的发送和接收密切相关。具体来说：
发送数据：当你需要通过 Netty 发送数据时，你通常会将数据写入 ByteBuf 中，然后将这个 ByteBuf 通过 Channel 发送出去。
接收数据：当 Netty 接收到数据时，数据会被读入一个 ByteBuf，然后你可以从 ByteBuf 中读取和处理数据。


源码位置：AbstractNioByteChannel  io.netty.channel.nio.AbstractNioByteChannel.filterOutboundMessage
过滤待发送的消息，只有ByteBuf（堆 or 非堆）以及 FileRegion可以进行最终的Socket网络传输，其他类型的数据是不支持的，会抛UnsupportedOperationException异常
并且会把堆ByteBuf转换为一个非堆的ByteBuf返回。也就说，最后会通过socket传输的对象时非堆的ByteBuf和FileRegion。