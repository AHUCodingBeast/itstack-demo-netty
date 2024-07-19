在我们使用netty中，需要监测服务是否稳定以及在网络异常链接断开时候可以自动重连。

自动重连 其实包含了三个个部分
（1）如果建立通道链接的时候就失败了 需要支持重连，
（2）如果通道已经建立了，后来通道关闭连接了或者异常掉线了 这时候客户端也要进行重连
（3）对于服务端和客户端如果需要维持长连接，服务端也要不停地呼应客户端 也就是空闲检测功能

下面大致讲下这个项目里面的关键逻辑：

客户端：
要点1：在客户端第一次链接服务端的时候给客户端额外绑定了一个监听：

//尝试与给定的主机和端口建立连接，并等待直到连接成功或失败。 如果连接成功，返回的ChannelFuture对象可以用来检查连接状态或注册回调函数。
ChannelFuture f = b.connect(inetHost, inetPort).sync();
//添加监听，处理重连，在MyChannelFutureListener类里 定义了如果通道链接失败的话的重连策略
f.addListener(new MyChannelFutureListener());

要点2：通道已经建立了，后来通道不活跃或者直接断线了，这时候怎么重连：
在MyClientHandler代码里的channelInactive 方法里进行重连




服务端：
要点1：在服务端的处理器流水线上 设置了一个空闲检测处理器
       /**
         * 心跳监测
         * 1、readerIdleTimeSeconds 读超时时间
         * 2、writerIdleTimeSeconds 写超时时间
         * 3、allIdleTimeSeconds    读写超时时间
         * 4、TimeUnit.SECONDS 秒[默认为秒，可以指定]
         * 下面这行代码的意思是：如果在2秒内没有读、写或读写操作，就会触发对应的空闲事件。
         * 在Netty框架中，IdleStateHandler是一个重要的处理器，主要用于检测通道两端长时间没有读、写或者读写操作的情况，即所谓的“空闲”状态。
         * 它的核心作用在于帮助实现长连接的心跳机制，这对于保持网络连接的健康状态至关重要，尤其是对于那些需要维持持久化连接的应用场景来说。
         * IdleStateHandler 在检测到空闲事件的时候 会触发一个IdleStateEvent事件，这个事件会被传递给管道中的下一个处理器（代码中在MyServerHandler进行了处理）。
         */
        channel.pipeline().addLast(new IdleStateHandler(2, 2, 2));

       正如上面的代码注释写的那样 IdleStateHandler触发一个IdleStateEvent事件之后在 MyServerHandler里进行了处理，具体的处理代码如下：主要就是想通道里面发消息给客户端

       public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.READER_IDLE) {
                    System.out.println("bugstack虫洞栈提醒=> Reader Idle");
                    ctx.writeAndFlush("读取等待：公众号bugstack虫洞栈，客户端你在吗[ctx.close()]{我结尾是一个换行符用于处理半包粘包}... ...\r\n");
                    ctx.close();
                } else if (e.state() == IdleState.WRITER_IDLE) {
                    System.out.println("bugstack虫洞栈提醒=> Write Idle");
                    ctx.writeAndFlush("写入等待：公众号bugstack虫洞栈，客户端你在吗{我结尾是一个换行符用于处理半包粘包}... ...\r\n");
                } else if (e.state() == IdleState.ALL_IDLE) {
                    System.out.println("bugstack虫洞栈提醒=> All_IDLE");
                    ctx.writeAndFlush("全部时间：公众号bugstack虫洞栈，客户端你在吗{我结尾是一个换行符用于处理半包粘包}... ...\r\n");
                }
            }
            ctx.flush();
        }
