知识点:
1、什么是数据出站 和数据入站
数据出站指的是数据从本地到网络，例如数据编码就是一个典型的出站场景 需要将我们发送的数据转为二进制字节发到网络
数据进站则指的是数据从网络到本地，例如数据解码就是一个典型的入站场景 需要将从网络接收到的二进制字节数据解码为我们需要的数据
如果你的应用场景主要是接收和处理来自网络的数据，而不需要主动向远端发送数据，
那么仅仅实现ChannelInboundHandlerAdapter就足够满足需求了。
ChannelInboundHandlerAdapter及其派生类主要聚焦于处理入站事件，也就是接收和解读从网络流入的数据。

2、处理器的处理顺序
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        // 基于换行符号
        channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
        // 解码转String，注意调整自己的编码格式GBK、UTF-8
        channel.pipeline().addLast(new StringDecoder(Charset.forName("GBK")));
        // 解码转String，注意调整自己的编码格式GBK、UTF-8
        channel.pipeline().addLast(new StringEncoder(Charset.forName("GBK")));
        channel.pipeline().addLast(new MyOutMsgHandler());
        channel.pipeline().addLast(new MyInMsgHandler());
    }
针对上面的例子
数据入站场景执行顺序分析
LineBasedFrameDecoder: 首先，从网络接收的原始字节流将通过LineBasedFrameDecoder进行帧定界。这个解码器基于换行符（默认为\r\n）将连续的字节流切分为独立的消息帧。
StringDecoder: 接下来，每个帧将通过StringDecoder进行解码，将其从字节数组转换成字符串形式，使用的字符集是GBK。
StringEncoder: 虽然StringEncoder在这里也被添加到了pipeline中，但在数据入站的路径上，它实际上并不会被调用，因为它主要用于将字符串编码为字节数组以便发送出去。不过，它的位置并不影响入站数据的处理流程。
MyOutMsgHandler: 类似于StringEncoder，MyOutMsgHandler作为出站处理器，它主要处理从本地应用程序到网络的数据传输。在数据入站的场景下，它同样不会参与处理。
MyInMsgHandler: 最终，经过前面解码和处理的数据将到达MyInMsgHandler，这是一个专门用来处理入站事件的处理器。一旦数据被解码并准备好供应用程序使用，MyInMsgHandler中的channelRead方法将会被调用，你可以在这里实现具体的业务逻辑。

数据出站场景执行顺序分析
LineBasedFrameDecoder和StringDecoder在这条出站路径上不会被调用，因为它们主要用于处理入站数据
对于数据出站的处理流程，事件处理器的执行顺序如下所示：
MyOutMsgHandler （首先执行）
StringEncoder  （次执行）
在数据从本地应用程序发出并最终发送到网络的过程中，MyOutMsgHandler和StringEncoder将起到关键作用，分别负责数据的预处理和编码。
而LineBasedFrameDecoder和StringDecoder则不会参与到这一流程中，因为它们是针对入站数据设计的。


3、处理器的单例模式（@ChannelHandler.Sharable）
当客户端连接到服务器时，Netty新建一个ChannelPipeline处理其中的事件，而一个ChannelPipeline中含有若干ChannelHandler。
如果每个客户端连接都新建一个ChannelHandler实例，当有大量客户端时，服务器将保存大量的ChannelHandler实例。
为此，Netty提供了Sharable注解，如果一个ChannelHandler状态无关，那么可将其标注为Sharable，如此，服务器只需保存一个实例就能处理所有客户端的事件。