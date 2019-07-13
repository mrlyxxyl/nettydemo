package net.ys.chat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 客户端
 */
public class NettyClient {

    public static void main(String[] args) throws Exception {
        startClient("127.0.0.1", 8899);
    }

    public static void startClient(String host, int port) throws IOException {
        //设置一个多线程循环器
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //启动附注类
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        //指定所使用的NIO传输channel
        bootstrap.channel(NioSocketChannel.class);
        //指定客户端初始化处理
        bootstrap.handler(new ClientInitHandler());
        try {
            //连接服务
            Channel channel = bootstrap.connect(host, port).sync().channel();
            while (true) {
                //向服务端发送内容
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String content = reader.readLine();
                if (StringUtils.isNotEmpty(content)) {
                    if (StringUtils.equalsIgnoreCase(content, "q")) {
                        System.exit(1);
                    }
                    channel.writeAndFlush(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}