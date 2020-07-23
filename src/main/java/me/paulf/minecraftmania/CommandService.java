package me.paulf.minecraftmania;

// https://netty.io/4.0/xref/io/netty/example/http/websocketx/client/WebSocketClient.html
public class CommandService implements Runnable {
    @Override
    public void run() {

    }

    public static CommandService create() {
        return new CommandService();
    }
}
