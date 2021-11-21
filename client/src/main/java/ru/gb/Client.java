package ru.gb;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class Client {
    private final int port;

    public static void main(String[] args) {
        try {
            new Client(9000).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Client(int port) {
        this.port = port;
    }

    private void start() throws InterruptedException {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LineBasedFrameDecoder(256),
                                    new StringEncoder(),
                                    new StringDecoder(),
                                    new SimpleChannelInboundHandler<String>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                            System.out.println(msg);
                                        }
                                    }
                            );
                        }
                    });
            // Start the client.
            Channel channel = bootstrap.connect("localhost", port).sync().channel();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                if ("q".equals(input)) {
                    break;
                }

                channel.writeAndFlush(input + System.lineSeparator());
            }
            channel.close();

            // Wait until the connection is closed.
            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
