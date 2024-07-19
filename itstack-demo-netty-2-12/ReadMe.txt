

流量整形（Traffic Shaping）是一种主动调整流量输出速率的措施。
一个典型应用是基于下游网络结点的TP指标来控制本地流量的输出。
流量整形与流量监管的主要区别在于，流量整形对流量监管中需要丢弃的报文进行缓存——通常是将它们放入缓冲区或队列内，也称流量整形（Traffic Shaping，简称TS）。
当令牌桶有足够的令牌时，再均匀的向外发送这些被缓存的报文。流量整形与流量监管的另一区别是，整形可能会增加延迟，而监管几乎不引入额外的延迟。


服务端的重点代码解释：
重点就是两个处理器：
GlobalTrafficShapingHandler：
GlobalTrafficShapingHandler 是 Netty 提供的一个用于流量整形（traffic shaping）的处理器。这类处理器用来控制流经 Channel 或 ChannelPipeline 中的数据流速率，从而防止网络带宽被个别连接占用，保持资源的公平使用。
主要功能
流量整形：控制传入和传出的流量速率，以避免网络过载。
全局流量控制：可以跨多个 Channel 控制流量。
动态调整：允许在运行时调整速率限制。
构造函数
GlobalTrafficShapingHandler 提供了多个重载的构造函数，主要参数如下：
executor: 用于执行定时任务的 EventExecutor，一般传入 EventLoopGroup。
writeLimit: 限制每秒写入的字节数，如果不需要限制，设置为 0。
readLimit: 限制每秒读取的字节数，如果不需要限制，设置为 0。
checkInterval: 检查流量的时间间隔，单位为毫秒。
maxTime: 每次调用 release 方法释放带宽的最长时间, 单位为毫秒

在本案例中
   channel.pipeline().addLast(new GlobalTrafficShapingHandler(channel.eventLoop().parent(), 10, 10)); 每秒写入的字节数为10 每秒读取的字节数为10



MyServerHandler：

在handlerAdded 方法中（这个方法在处理器加入处理器链的时候就会触发） 定义了一个任务 counterTask 这个任务的作用就是