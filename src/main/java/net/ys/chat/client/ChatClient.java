package net.ys.chat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * User: NMY
 * Date: 2019-7-15
 * Time: 9:03
 */
public class ChatClient {

    public static void main(String[] args) {
        startClient("localhost", 8080);
    }

    private static void startClient(String host, int port) {
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast("http", new HttpClientCodec());
                    pipeline.addLast("chat", new ChatClientHandler());
                }
            });

            Channel channel = bootstrap.connect(host, port).sync().channel();
            while (true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String content = reader.readLine();
                if (StringUtils.isNotEmpty(content)) {
                    if (StringUtils.equalsIgnoreCase(content, "q")) {
                        System.exit(1);
                    } else {
                        channel.writeAndFlush(content);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    private static class ChatClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println(msg);
        }
    }
}
