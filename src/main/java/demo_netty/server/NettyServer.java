package demo_netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class NettyServer {

    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        // 创建EventLoopGroup用于处理I/O操作
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // 处理连接的线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 处理I/O的线程组
        try {
            // 创建Bootstrap
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 使用NIO传输
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 为每个客户端新建一个channel
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                // 读客户端发来的消息
                                @Override
                                protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, String msg) {
                                    System.out.println("Received message: " + msg);
                                    // 回应客户端
                                    ctx.writeAndFlush(Unpooled.copiedBuffer("Hello from Server!", CharsetUtil.UTF_8));
                                }

                                // 链接客户端成功时做的动作
                                @Override
                                public void channelActive(io.netty.channel.ChannelHandlerContext ctx) {
                                    System.out.println("Client connected: " + ctx.channel().remoteAddress());
                                }
                            });
                        }
                    });

            // 绑定端口并启动
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Server started on port: " + port);
            future.channel().closeFuture().sync(); // 等待通道关闭
        } finally {
            // 优雅退出
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyServer(8080).start(); // 启动服务端
    }
}
