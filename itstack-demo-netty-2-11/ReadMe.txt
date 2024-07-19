在Netty这种异步NIO框架的结构下，服务端与客户端通信过程中，高效、频繁、大量的写入大块数据时，因网络传输饱和的可能性就会造成数据处理拥堵、GC频繁、用户掉线的可能性。
那么由于写操作是非阻塞的，所以即使没有写出所有的数据，写操作也会在完成时返回并通知ChannelFuture。
当这种情况发生时，如果仍然不停地写入，就有内存耗尽的风险。所以在写大块数据时，需要对大块数据进行切割发送处理



本案例的要点如下：

首先我们知道如果服务端要给客户端一个非常大的数据包的话 不可能说一下子就全部给到客户端，所以我们需要将大数据分割成小数据包进行发送，
在服务端的处理器链上面有两个关键节点
    channel.pipeline().addLast(new ChunkedWriteHandler());
    channel.pipeline().addLast(new MyServerChunkHandler());

ChunkedWriteHandler 是netty自带的出站以及入站处理器，它的作用就是会将大数据分割成小数据包进行发送
ChunkedWriteHandler 是 Netty 提供的一种特殊处理器，用于处理分片的大数据流。当你要发送的数据非常大以至于一次性放入内存可能会导致性能问题或内存溢出时，ChunkedWriteHandler 就派上了用场。
作用：它允许你将大数据流拆分成较小的部分(chunk)，逐个写入网络中而无需一次性加载全部数据到内存中。
应用场景：特别适用于HTTP/HTTPS协议下的大文件上传下载、视频流传输等场景。
内部机制：它支持多种数据源的分片处理，如文件输入流、字节数组等，并自动管理数据分片的序列化和发送。

MyServerChunkHandler 是我们自定义的处理器，数据经过ChunkedWriteHandler会变成很多的小数据包ByteBuf，然后这个出站处理器就是从ByteBuf中读取数据转为ChunkedStream对象stream，设置每次读取的数据量为10字节。
调用ctx.write(stream, promise)完成向客户端的数据发送，此时这个处理器还为这次发送指定了一个ChannelProgressivePromise 对象，
这个对象可以注册回调函数，回调函数中可以获取当前的发送进度以及发送结果

最后因为我们这个出站处理器处理的数据类型是ByteBuf  这个有可能在服务器创建的时候指定的是堆外内存，如下面所示 所以好的习惯是释放msg （ ReferenceCountUtil.release(msg);）因为后续的出站处理器已经用不到这个对象了


小知识 关于ByteBuf的堆外内存的概念：
在Netty中，ByteBuf是用于处理字节序列的核心抽象类，它可以基于堆内或堆外内存。默认情况下，Netty倾向于使用堆外内存，
即Unpooled.wrappedBuffer(ByteBuffer)或PooledByteBufAllocator.DEFAULT.directBuffer(int)等方法创建的ByteBuf实例。

Netty的堆外内存使用可以通过配置EventLoopGroup的ByteBufAllocator来控制。例如，你可以使用EpollEventLoopGroup或NioEventLoopGroup时，传递一个PooledByteBufAllocator实例，如下所示：
EventLoopGroup group = new NioEventLoopGroup(1, new DefaultThreadFactory("my-event-loop"),  new PooledByteBufAllocator(true));
这里的true参数指示使用堆外内存。


