package org.example.RESTful.servers;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NaiveServer {
    private static final int port = 8080;

    public static void main(String[] args) throws IOException {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Naive Server Started on port " + port);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid())
                        continue;

                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if ((key.isReadable())) {
                        SocketChannel client = (SocketChannel) key.channel();
                        handleRESTfulRequest(key, client);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRESTfulRequest(SelectionKey key, SocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        try {
            int bytesRead = client.read(buffer);

            if (bytesRead == -1) {
                client.close();
                key.cancel();
                return;
            }

            if (bytesRead > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                String requestString = new String(data, StandardCharsets.UTF_8);

                Socket fakeSocket = new Socket() {
                    @Override
                    public InputStream getInputStream() {
                        return new ByteArrayInputStream(requestString.getBytes(StandardCharsets.UTF_8));
                    }

                    @Override
                    public OutputStream getOutputStream() {
                        return new OutputStream() {
                            @Override
                            public void write(int b) throws IOException {
                                ByteBuffer outBuf = ByteBuffer.allocate(1);
                                outBuf.put((byte) b);
                                outBuf.flip();
                                client.write(outBuf);
                            }

                            @Override
                            public void write(byte[] b, int off, int len) throws IOException {
                                ByteBuffer outBuf = ByteBuffer.wrap(b, off, len);
                                client.write(outBuf);
                            }
                        };
                    }
                };

                NaiveGameController.handle(fakeSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (client.isOpen())
                    client.close();
            } catch (IOException e) {
            }
            key.cancel();
        }
    }
}
