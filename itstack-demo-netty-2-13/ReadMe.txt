SSL(Secure Sockets Layer 安全套接层),及其继任者传输层安全（Transport Layer Security，TLS）是为网络通信提供安全及数据完整性的一种安全协议。TLS与SSL在传输层对网络连接进行加密。

在实际通信过程中，如果不使用SSL那么信息就是明文传输，从而给非法分子一些可乘之机；
窃听风险[eavesdropping]：第三方可以获知通信内容。
篡改风险[tampering]：第三方可以修改通信内容。
冒充风险[pretending]：第三方可以冒充他人身份参与通信。
SSL/TLS协议就是为了解决这三大风险而设计的；

保密：在握手协议中定义了会话密钥后，所有的消息都被加密。
鉴别：可选的客户端认证，和强制的服务器端认证。
完整性：传送的消息包括消息完整性检查（使用MAC）。
那么本章节我们通过在netty的channHandler中添加SSL安全模块{sslContext.newHandler(channel.alloc())}，来实现加密传输的效果。