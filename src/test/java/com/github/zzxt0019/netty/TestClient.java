package com.github.zzxt0019.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.function.Supplier;

public class TestClient {

    public static void sendStr(Supplier<String[]> supplier) {
        String[] strings = supplier.get();
        byte[][] bytes = new byte[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            bytes[i] = strings[i].getBytes();
        }
        send(() -> bytes);
    }

    public static void send(Supplier<byte[][]> supplier) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            new Bootstrap()
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    for (byte[] bytes : supplier.get()) {
                                        Thread.sleep(100);
                                        System.out.println(Arrays.toString(bytes));
                                        ctx.writeAndFlush(Unpooled.wrappedBuffer(bytes));
                                    }
                                }
                            });
                        }
                    })
                    .connect(new InetSocketAddress("localhost", 9998))
                    .sync();
        } catch (InterruptedException e) {
            worker.shutdownGracefully();
        }
    }
}
