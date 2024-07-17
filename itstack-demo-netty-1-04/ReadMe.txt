
EventLoopGroup parentGroup = new NioEventLoopGroup();
EventLoopGroup childGroup = new NioEventLoopGroup();
ServerBootstrap b = new ServerBootstrap();
         b.group(parentGroup, childGroup)
                 .channel(NioServerSocketChannel.class)    //非阻塞模式
                 .option(ChannelOption.SO_BACKLOG, 128)
                 .childHandler(new MyChannelInitializer());

问题1： 为什么需要两个EventLoop线程组

 分离职责：通过将连接接受 和 连接处理的职责分开，可以使服务器更加健壮和可扩展。
 parentGroup专注于快速处理连接请求，而childGroup则专注于高效处理大量并发连接的数据传输。
 负载均衡：childGroup中的多个EventLoop线程可以平衡处理来自不同客户端的I/O操作，提高系统的整体吞吐量和响应速度。

 Parent Group (parentGroup)
 职责：主要负责接受客户端的连接请求，创建新的Channel并与客户端建立连接。一旦连接建立，parentGroup就不再参与后续的I/O操作。
 线程数：一般较少，因为它的任务相对简单，主要是监听和接受连接。

 Child Group (childGroup)
 职责：负责处理已经建立的连接的所有读写操作，包括数据的读取、写入、编码、解码等。
 每一个由parentGroup接受的连接都会被分配给childGroup中的某个EventLoop线程进行后续处理。
 线程数：通常较多，以便能够高效地处理多个并发连接的I/O操作。

 问题1： 参数SO_BACKLOG 是什么含义？
 SO_BACKLOG 参数是TCP/IP协议栈中的一个选项，主要用于控制操作系统层面的未完成连接队列的大小。当我们创建一个服务器套接字并调用listen()方法时，服务器就开始监听特定的IP地址和端口号上到来的连接请求。当客户端试图与服务器建立连接时，它发送一个SYN数据包给服务器，服务器收到这个SYN数据包后，会将其放入未完成连接队列中，并发送一个SYN+ACK数据包回给客户端。
 此时，连接还处于半打开状态，即所谓的三次握手还未完成。
 SO_BACKLOG参数就是用来设定这个未完成连接队列的最大长度。具体来说：
 当队列满了之后，再有新的连接请求到达，就会被操作系统丢弃，这会导致客户端无法与服务器建立连接。
 较大的SO_BACKLOG值可以让更多的连接请求排队等待，但同时也占用更多的内存资源。
 这个值并不是越大越好，因为它受到操作系统限制，而且过大的值可能会浪费内存资源。
 在大多数现代操作系统中，SO_BACKLOG的实际有效值会被限制在一定范围内，比如Linux中通常不超过128或更大的值取决于系统配置。
 因此，设置一个合理的SO_BACKLOG值是很重要的，既要保证足够的连接请求可以排队，又不至于浪费太多资源。
