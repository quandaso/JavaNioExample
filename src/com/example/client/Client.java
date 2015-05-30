package com.example.client;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class Client {
    private String host;
    private int port;
    private SocketChannel socket;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws Exception {
        socket = SocketChannel.open();
        socket.configureBlocking(true);
        socket.connect(new InetSocketAddress(host, port));
    }

    public void disconnect() throws Exception {
        socket.close();
    }

    public void send(String s) {
        ByteBuffer buffer = ByteBuffer.wrap(s.getBytes());
        try {
            socket.write(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Receives server response
     * */
    public String receive() throws IOException{
        ByteBuffer rpHeader= ByteBuffer.allocateDirect(1024);
        socket.read(rpHeader);
        rpHeader.flip();
        byte[] headerBytes = new byte[rpHeader.remaining()];
        rpHeader.get(headerBytes);
        return new String(headerBytes, "UTF-8");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8881;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        Client client = new Client(host, port);
        client.connect();
        client.send("Hello server");
        String receivedMsg = client.receive();
        System.out.println(receivedMsg);
        client.disconnect();
    }

}
