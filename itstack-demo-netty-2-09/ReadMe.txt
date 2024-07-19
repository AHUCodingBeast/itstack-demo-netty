Netty的性能非常好，在一些小型用户体量的socket服务内，仅部署单台机器就可以满足业务需求。
但当遇到一些中大型用户体量的服务时，就需要考虑讲Netty按照集群方式部署，以更好的满足业务诉求。
但Netty部署集群后都会遇到跨服务端怎么通信?
也就是有集群服务X和Y，用户A链接服务X，用户B链接服务Y，那么他们都不在一个服务内怎么通信？

跨服务之间案例采用redis的发布和订阅进行传递消息，如果你是大型服务可以使用zookeeper
用户A在发送消息给用户B时候，需要传递B的channeId，以用于服务端进行查找channeId所属是否自己的服务内
单台机器也可以启动多个Netty服务，程序内会自动寻找可用端

接下来终点讲解下这个项目中的要点：
服务端：
（1）NettyController#openNettyServer 中能够自动获取当前可用端口然后启动一个Netty的服务器
启动服务器以后 会将服务器和端口作为映射 存进 CacheUtil.serverMap 中
同时也会把服务器的基本信息（IP，端口，开启时间）存到  CacheUtil.serverInfoMap 中

（2） 通过NettyController#queryNettyServerList 可以获取目前的服务器列表 （主要就是读取CacheUtil.serverInfoMap 里面的信息），
     因为CacheUtil.serverMap 存放着所有本地Netty服务器所以也可以通过这个类进行netty服务器的启停管理

（3）在（1）中启动的netty服务器中的处理器链上面有个MyServerHandler，在这个处理器里面做了下面几个关键逻辑：
- 在channelActive 方法中记录下活跃的 当前ChannelID和服务器的Ip地址以及通道的连接时间，然后将这个通道的基础信息给记录到Redis上面，
  然后在本地还维护一份映射 CacheUtil.cacheChannel 里面的Key是通道的ID value就是通道
- 在通道可读的时候会触发channelRead方法里面的逻辑，在本案例中客户端发送过来的实际上是MsgAgreement对象，这个对象里面有消息需要发送到的目标ChannelId
  因为客户端发送过来的消息对象中有目标ChannelID于是就可以读取CacheUtil.cacheChannel里面的内容 看看能不能取出来通道用来给客户端响应数据，如果能找到就往通道写入数据
  如果说这个内存映射找不到 这说明本机的服务端没有这个通道，说明当前机器处理不了这个客户请求，于是直接把这个用户消息转发到Redis上面（利用redis的发布订阅功能）

（4）在springBoot容器启动的时候，会注册ReceiverConfig 配置类 在这个类里面建立了与redis的订阅关系 代码如下：
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter msgAgreementListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(msgAgreementListenerAdapter, new PatternTopic("itstack-demo-netty-push-msgAgreement"));
        return container;
    }
（5）实际订阅 itstack-demo-netty-push-msgAgreement 的 消息处理逻辑代码在 MsgAgreementReceiver 类中，这个类定义了收到第三步的消息后如何处理
因为redis的发布订阅是一种广播模式，只要是发布了消息所有订阅的客户端都会收到消息，消息的处理也非常简单
就是取出消息里面的目标ChannelId然后去检查本地的CacheUtil.cacheChannel里面的内容
看看能不能取出来通道用来给客户端响应数据，如果能找到就往通道写入数据，这样客户端就能收到数据了


总结： 本质上就是利用redis中间件的发布订阅功能完成了跨服务端的通信


