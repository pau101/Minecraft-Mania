package me.paulf.minecraftmania;

import com.google.common.util.concurrent.MoreExecutors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.TimeUnit;

public final class OkHttpSocketTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .build();
        final Request request = new Request.Builder()
            .url("ws://localhost:8080")
            .build();
        client.newWebSocket(request, new MyListener());
        MoreExecutors.shutdownAndAwaitTermination(client.dispatcher().executorService(), Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    // https://blog.jakubholy.net/2007/11/02/truncating-utf-string-to-the-given/
    private static String truncate(final String s, final Charset charset, final int length) {
        final ByteBuffer bb = ByteBuffer.wrap(s.getBytes(charset), 0, length);
        final CharBuffer cb = CharBuffer.allocate(length);
        final CharsetDecoder cd = charset.newDecoder();
        cd.onMalformedInput(CodingErrorAction.IGNORE);
        if (!cd.decode(bb, cb, true).isUnderflow()) {
            throw new AssertionError();
        }
        if (!cd.flush(cb).isUnderflow()) {
            throw new AssertionError();
        }
        return new String(cb.array(), 0, cb.position());
    }

    static class MyListener extends WebSocketListener {
        @Override
        public void onOpen(final WebSocket webSocket, final Response response) {
            LOGGER.info("Opened");
            webSocket.send("Hello!");
        }

        @Override
        public void onMessage(final WebSocket webSocket, final String text) {
            LOGGER.info("Received text message {}", text);
        }

        @Override
        public void onMessage(final WebSocket webSocket, final ByteString bytes) {
            LOGGER.info("Received binary message {}", bytes.toAsciiLowercase());
        }

        @Override
        public void onClosing(final WebSocket webSocket, final int code, final String reason) {
            LOGGER.info("Closing");
            webSocket.close(1000, "Closing");
        }

        @Override
        public void onClosed(final WebSocket webSocket, final int code, final String reason) {
            LOGGER.info("Closed");
        }

        @Override
        public void onFailure(final WebSocket webSocket, final Throwable t, @Nullable final Response response) {
            LOGGER.warn("Failure", t);
        }
    }
}
