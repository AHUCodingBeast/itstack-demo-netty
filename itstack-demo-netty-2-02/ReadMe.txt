在netty数据传输过程中可以有很多选择，比如；字符串、json、xml、java对象，

但为了保证传输的数据具备；良好的通用性、方便的操作性和传输的高性能，我们可以选择protobuf作为我们的数据传输格式。


目前protobuf可以支持；C++、C#、Dart、Go、Java、Python等，也可以在JS里使用。知识点；ProtobufDecoder、ProtobufEncoder、ProtobufVarint32FrameDecoder、ProtobufVarint32LengthFieldPrepender。