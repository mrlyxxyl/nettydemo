package net.ys.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * User: NMY
 * Date: 2019-7-15
 * Time: 9:03
 */
public class ChatServer {

    public static void main(String[] args) {
        startServer(8080);
    }

    private static void startServer(int port) {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast("chat", new ChatServerHandler());
                }
            });

            Channel channel = serverBootstrap.bind(port).sync().channel();
            System.out.println("server started at port:" + port);
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private static class ChatServerHandler extends SimpleChannelInboundHandler<String> {

        private static final ChannelGroup GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            Channel channel = ctx.channel();
            for (Channel ch : GROUP) {

                if (ch == channel) {
                    ch.writeAndFlush("[you]: " + msg + "\n");
                } else {
                    ch.writeAndFlush("[" + ch.remoteAddress() + "]: " + msg + "\n");
                }
            }

            System.out.println("[" + channel.remoteAddress() + "]: " + msg + "\n");
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            GROUP.add(ctx.channel());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            GROUP.remove(ctx.channel());
        }

    }
}
