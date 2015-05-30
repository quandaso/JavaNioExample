package com.example.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    public String host;
    public int port;
    public Selector selector;
    public ServerSocketChannel server;
    public ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    public SocketChannel currentClient;
    public ConcurrentHashMap<String, SocketChannel> sockets = new ConcurrentHashMap<String, SocketChannel>();
    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Listens to clients
     *
     */
    public void listen() {

        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(this.host, this.port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server " + host + " start at port " + port + "...");

            while (true) {
                Thread.sleep(1);
                selector.select();
                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    SelectionKey key = i.next();
                    i.remove();

                    if (key.isConnectable()) {
                        ((SocketChannel)key.channel()).finishConnect();
                    }

                    if (key.isAcceptable()) {
                        // accept connection
                        currentClient = server.accept();
                        currentClient.configureBlocking(false);
                        currentClient.socket().setTcpNoDelay(true);
                        currentClient.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println(currentClient.getRemoteAddress() + " connected...");
                    }

                    if (key.isReadable()) {
                        // ...read messages...
                        currentClient = (SocketChannel) key.channel();

                        buffer.clear();
                        int numBytesRead = -1;

                        try {
                            numBytesRead = currentClient.read(buffer);
                        } catch (Exception e) {
                            System.out.println("Client has been disconnected");
                        }

                        if (numBytesRead == -1) {
                            // No more bytes can be read from the channel
                            currentClient.close();
                        } else {
                            try {
                                byte[] receivedBytes = getReceivedBytes(buffer, numBytesRead);
                                System.out.println("Client: " + new String(receivedBytes));
                                send(currentClient, "Server: Hello client".getBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets receive byte
     * @param buffer
     * @param length
     * @return
     */
    public static byte[] getReceivedBytes(ByteBuffer buffer, int length) {
        buffer.flip();
        byte[] receivedBytes = new byte[length];
        for (int j = 0; j < receivedBytes.length; j++) {
            receivedBytes[j] = buffer.get(j);
        }
        return receivedBytes;
    }

    /**
     * Sends bytes via socket channel
     * @param sc
     * @param bytes
     */
    public boolean send(SocketChannel sc, byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        try {
            while (buffer.hasRemaining()) {
                sc.write(buffer);
            }

            return true;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String host = "localhost";
        int port = 8881;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        new Server(host, port).listen();
    }

}
