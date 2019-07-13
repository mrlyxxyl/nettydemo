package net.ys.chat.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 服务器初始化
 */
public class ServerInitHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //管道注册handler
        ChannelPipeline pipeline = socketChannel.pipeline();
        //编码通道处理
        pipeline.addLast("decode", new StringDecoder());
        //转码通道处理
        pipeline.addLast("encode", new StringEncoder());
        //聊天服务通道处理
        pipeline.addLast("chat", new ServerHandler());
    }
}