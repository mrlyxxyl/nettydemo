package net.ys.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * User: NMY
 * Date: 2019-7-13
 * Time: 14:36
 */
public class ClientDemo {

    public static void main(String[] args) {
        startClient("localhost", 8080);
    }

    private static void startClient(String host, int port) {

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DemoClientHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            System.out.println("client started and send msg");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    private static class DemoClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Thread.sleep(2000);
            this.channelActive(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String reqMsg = "hello server, i am a client-->" + System.currentTimeMillis();
            byte[] bytes = reqMsg.getBytes("utf-8");
            ByteBuf buffer = Unpooled.buffer(bytes.length);
            buffer.writeBytes(bytes);
            ctx.writeAndFlush(buffer);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            String body = new String(bytes, "utf-8");
            System.out.println("server resp msg:--->" + body);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("exception msg :" + cause.getMessage());
            ctx.close();
        }
    }
}
