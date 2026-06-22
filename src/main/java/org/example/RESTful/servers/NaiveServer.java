package org.example.RESTful.servers;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class NaiveServer {
    private static final int port = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/game/turn", new NaiveGameController());

        server.setExecutor(null);

        System.out.println("Naive Server Started on port " + port);
        server.start();
    }
}
