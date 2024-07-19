在我们实际做应用级开发的过程中，客户端与服务端需要发送多种消息类型

比如一个聊天室场景包括的消息类型；登录验证、组建群聊、发送消息、退出登录等等，但如果我们都是用统一对象加if判断来分别转换，那么对后期的维护成本就会非常大，这样的代码方式也不是一个面向对象开发的思维。

面向对象的开发思路，经常会把很多if、switch等逻辑抽象成对应的接口和抽象类，以及加入工厂方式对服务进行动态编排。

那么我们在这里也同样需要定义一个抽象类，抽象类里包含了一个必须实现的标识性属性，用来编码解码时提取标识，找到对应的处理类进行操作。

这样我们就可以不断的去扩展我们需要的不同维度的消息处理的Handler，在这个案例里我们模拟了；demo01、demo02、demo03三组消息处理handler，他们都统一继承抽象类Packet，并实现里面的getCommand方法。

另外可以在这个抽象类中加入一些其他属性，包括；版本、校验、加密等，可以更加方便的用于处理各类通用非业务属性逻辑行为。



重点代码解释：

这个项目整体比较简单，我们先看客户端：
客户端：
1、客户端在启动并成功链接上通道之后就可以发送信息，这个案例里面定义了三种消息类分别为MsgDemo01 MsgDemo02 MsgDemo03 这三个类都实现了Packet接口，并重写了getCommand方法，用来获取消息的类型。
2、在ObjDecoder类里面也就是出站处理器里面定义了消息传输协议 在转换为字节流的时候实际上是三个部分
   第一部分四个字节，代表数据的总长度（即第二部分和第三部分的总长度）
   第二部分插入一bit位，表示消息类型
   第三部分是消息内容
3、在ObjDecoder 里面通过提取第二部分的消息类型，反序列化为具体的消息对象（MsgDemo01 或 MsgDemo02 或 MsgDemo03 ）


服务端：
服务端的重点是我们的处理器流水线上面三个处理器：
    protected void initChannel(SocketChannel channel) {
        //对象传输处理[解码]
        channel.pipeline().addLast(new ObjDecoder());
        // 在管道中添加我们自己的接收数据实现方法
        channel.pipeline().addLast(new MsgDemo01Handler());
        channel.pipeline().addLast(new MsgDemo02Handler());
        channel.pipeline().addLast(new MsgDemo02Handler());
        //对象传输处理[编码]
        channel.pipeline().addLast(new ObjEncoder());
    }
 上面已经说过ObjDecoder 可以分析二进制里面的消息类型 反序列化为具体的消息对象 ，因为这个处理器被加到了入站的第一个处理器上面 经过他处理以后消息类型就能确定下来了
 MsgDemo01Handler和 MsgDemo02Handler 以及MsgDemo02Handler都继承了netty框架里面的SimpleChannelInboundHandler<T> 通过指定了数据类型T，来处理不同类型的消息

 所以这样就做到了一种消息类型只会被这三个消息处理器当中的一个进行处理的目的



