package me.paulf.minecraftmania;

import com.google.common.base.Throwables;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class CommandService implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(CommandService.class);

    private final URI uri;
    private final String host;
    private final int port;
    private final HttpHeaders headers;

    public CommandService(final URI uri, final String host, final int port, final HttpHeaders headers) {
        this.uri = uri;
        this.host = host;
        this.port = port;
        this.headers = headers;
    }

    public void stop() {

    }

    @Override
    public void run() {
        final SslContext ssl;
        if ("wss".equals(this.uri.getScheme())) {
            try {
                ssl = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // FIXME: security
                    .build();
            } catch (final SSLException e) {
                throw new RuntimeException(e);
            }
        } else {
            ssl = null;
        }
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
            while (!group.isShuttingDown()) {
                final WebSocketClientHandler handler = new WebSocketClientHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(this.uri, WebSocketVersion.V13, null, false, this.headers)
                );
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) {
                        final ChannelPipeline p = ch.pipeline();
                        if (ssl != null) {
                            p.addLast(ssl.newHandler(ch.alloc(), CommandService.this.host, CommandService.this.port));
                        }
                        p.addLast(new HttpClientCodec(), new HttpObjectAggregator(0x2000), handler);
                    }
                });
                LOGGER.info("Connecting to {}", this.uri);
                final ChannelFuture connect = b.connect(this.host, this.port);
                if (await(connect, "connect")) continue;
                final Channel ch = connect.channel();
                if (await(handler.handshakeFuture(), "handshake")) continue;
                // Send usable commands
                ch.writeAndFlush(new TextWebSocketFrame("TODO"));
                // loop sending command future completions
            }
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static boolean await(final ChannelFuture future, final String message) throws InterruptedException {
        future.await();
        if (future.isCancelled()) throw new InterruptedException("Cancelled " + message);
        if (future.isSuccess()) return false;
        final Throwable t = future.cause();
        Throwables.throwIfUnchecked(t);
        if (t.getCause() instanceof SocketException) {
            LOGGER.warn("{}", t.getMessage());
        } else {
            LOGGER.error("Error during " + message, t);
        }
        return true;
    }

    public static void main(final String[] args) {
        final CommandService service = create(URI.create("ws://localhost"), "undefined");
        service.run();
    }

    public static CommandService create(final URI uri, final String token) {
        final String scheme = uri.getScheme();
        final int uriPort = uri.getPort();
        final int port;
        if ("ws".equals(scheme)) {
            port = uriPort == -1 ? 80 : uriPort;
        } else if ("wss".equals(scheme)) {
            port = uriPort == -1 ? 443 : uriPort;
        } else if (scheme == null) {
            throw new NullPointerException("scheme");
        } else {
            throw new IllegalArgumentException("Unsupported scheme: " + scheme);
        }
        final String host = uri.getHost();
        if (host == null) {
            throw new NullPointerException("host");
        }
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.AUTHORIZATION, "Bearer " + token);
        return new CommandService(uri, host, port, headers);
    }

    public static class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
        private final WebSocketClientHandshaker handshaker;

        private ChannelPromise handshakeFuture;

        public WebSocketClientHandler(final WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
        }

        public ChannelFuture handshakeFuture() {
            return this.handshakeFuture;
        }

        @Override
        public void handlerAdded(final ChannelHandlerContext ctx) {
            this.handshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(final ChannelHandlerContext ctx) {
            this.handshaker.handshake(ctx.channel());
        }

        @Override
        public void channelInactive(final ChannelHandlerContext ctx) {
            System.out.println("WebSocket Client disconnected!");
        }

        @Override
        public void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            final Channel ch = ctx.channel();
            if (!this.handshaker.isHandshakeComplete()) {
                if (!(msg instanceof FullHttpResponse)) {
                    if (msg instanceof TextWebSocketFrame) {
                        throw new IllegalStateException("Unexpected TextWebSocketFrame{text=" + ((TextWebSocketFrame) msg).text() + '}');
                    }
                    throw new IllegalStateException("Unexpected " + msg.getClass().getSimpleName());
                }
                this.handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                this.handshakeFuture.setSuccess();
                return;
            }
            if (!(msg instanceof WebSocketFrame)) {
                if (msg instanceof FullHttpResponse) {
                    final FullHttpResponse response = (FullHttpResponse) msg;
                    throw new IllegalStateException("Unexpected FullHttpResponse{state=" + response.status() +
                        ", content=" + response.content().toString(StandardCharsets.UTF_8) + '}');
                }
                throw new IllegalStateException("Unexpected " + msg.getClass().getSimpleName());
            }
            final WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame) {
                final TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                System.out.println("WebSocket Client received message: " + textFrame.text());
            } else if (frame instanceof PongWebSocketFrame) {
                System.out.println("WebSocket Client received pong");
            } else if (frame instanceof CloseWebSocketFrame) {
                ch.close();
            }
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            cause.printStackTrace();
            if (!this.handshakeFuture.isDone()) {
                this.handshakeFuture.setFailure(cause);
            }
            ctx.close();
        }
    }
}