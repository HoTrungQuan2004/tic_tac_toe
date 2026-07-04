package org.example.HTTP.Servers;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerSide {
    private static final int port = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/game/turn", new HttpGameController());

        server.setExecutor(null);

        System.out.println("Naive Server Started on port " + port);
        server.start();
    }
}