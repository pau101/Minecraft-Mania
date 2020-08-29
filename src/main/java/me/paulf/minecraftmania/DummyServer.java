package me.paulf.minecraftmania;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.logging.log4j.LogManager;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public final class DummyServer {
    public static void main(final String[] args) {
        final SslContext sslCtx;
        if (false) {
            final SelfSignedCertificate ssc;
            try {
                ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } catch (final CertificateException | SSLException e) {
                throw new RuntimeException(e);
            }
        } else {
            sslCtx = null;
        }
        final String PATH = "/";
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) throws Exception {
                        final ChannelPipeline pipeline = ch.pipeline();
                        if (sslCtx != null) {
                            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
                        }
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(0x2000));
                        pipeline.addLast(new WebSocketServerCompressionHandler());
                        pipeline.addLast(new WebSocketServerProtocolHandler(PATH, null, true));
                        //pipeline.addLast(new WebSocketIndexPageHandler(PATH));
                        pipeline.addLast(new WebSocketServerHandler());
                    }
                });
            final Channel ch = b.bind(80).sync().channel();
            LogManager.getLogger().info("Server opened");
            ch.closeFuture().sync();
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final WebSocketFrame msg) throws Exception {
            if (msg instanceof TextWebSocketFrame) {
                final String text = ((TextWebSocketFrame) msg).text();
                System.out.printf("RECEIVED %s %n", text);
            }
        }
    }
}
