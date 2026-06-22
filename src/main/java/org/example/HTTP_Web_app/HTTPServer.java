package org.example.HTTP_Web_app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPServer {
    private static final int PORT = 8080;

    // /game/<uuid>/move
    private static final Pattern MOVE_PATH = Pattern.compile("^/game/([^/]+)/move$");
    // /game/<uuid>
    private static final Pattern SESSION_PATH = Pattern.compile("^/game/([^/]+)$");

    private final GameSessionManager manager = new GameSessionManager();

    public static void main(String[] args) throws IOException {
        new HTTPServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", this::dispatch);

        server.setExecutor(null);
        server.start();

        System.out.println("HTTP Server started. Listening on port " + PORT);
        System.out.println("POST /game          – start a new game");
        System.out.println("GET  /game/{id}     – get game state");
        System.out.println("POST /game/{id}/move – make a move");
        System.out.println("DELETE /game/{id}   – end session");
    }

    private void dispatch(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equalsIgnoreCase(method)) {

                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/game".equals(path)) {
                handleCreateGame(exchange);
            } else if ("GET".equalsIgnoreCase(method) && SESSION_PATH.matcher(path).matches()) {
                String id = extractGroup(SESSION_PATH, path, 1);
                handleGetGame(exchange, id);
            } else if ("POST".equalsIgnoreCase(method) && MOVE_PATH.matcher(path).matches()) {
                String id = extractGroup(MOVE_PATH, path, 1);
                handleMove(exchange, id);
            } else if ("DELETE".equalsIgnoreCase(method) && SESSION_PATH.matcher(path).matches()) {
                String id = extractGroup(SESSION_PATH, path, 1);
                handleDelete(exchange, id);
            } else {
                sendJson(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }


    // ========================================
    // HANDLERS
    // ========================================
    private void handleCreateGame(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);

        // humanFirst default true if absent in json
        boolean humanFirst = true;
        if (body.contains("\"humanFirst\"")) {
            humanFirst = body.contains("\"humanFirst\":true")
                        || body.contains("\"humanFirst\": true");
        }

        GameSession session = manager.createGameSession(humanFirst);

        String msg = "Hello!";

        sendJson(exchange, 201, buildStateJson(session, msg));
    }

    private void handleGetGame(HttpExchange exchange, String id) throws IOException {
        GameSession session = manager.getSession(id);
        if (session == null) {
            sendJson(exchange, 404, "{\"ERROR\":\"Session not found\"}");
            return;
        }
        String msg = turnMessage(session);
        sendJson(exchange, 200, buildStateJson(session, msg));
    }

    private void handleMove(HttpExchange exchange, String id) throws IOException {
        GameSession session = manager.getSession(id);
        if (session == null) {
            sendJson(exchange, 404, "{\"ERROR\":\"Session not found\"}");
            return;
        }

        String body = readBody(exchange);
        int pos;
        try {
            pos = parseIntField(body, "position");
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"error\":\"Body must contain \\\"position\\\" (1-9)\"}");
            return;
        }

        String error = session.humanMove(pos);
        if (error != null) {
             sendJson(exchange, 400, "{\"ERROR\":\"" + escapeJson(error) + "\"}");
             return;
        }

        String msg = switch (session.getStatus()) {
            case HUMAN_WIN -> "Player 1 win";
            case AI_WIN ->  "Player 2 win";
            case DRAW ->  "Draw";
            default -> turnMessage(session);
        };

        sendJson(exchange, 200, buildStateJson(session, msg));
    }

    private void handleDelete(HttpExchange exchange, String id) throws IOException {
        if (manager.getSession(id) == null) {
            sendJson(exchange, 404, "{\"ERROR\":\"Session not found\"}");
            return;
        }
        manager.removeSession(id);
        sendJson(exchange, 200, "{\"message\":\"Session removed\"}");
    }

    // ===========================
    // JSON
    // ===========================
    private String buildStateJson(GameSession session, String message) {
        int[] board = session.getSnapshot();
        String boardArr = Arrays.toString(board);

        return "{"
                + "\"gameId\":\"" + session.getId() + "\","
                + "\"board\":" + boardArr + ","
                + "\"status\":\"" + session.getStatus() + "\","
                + "\"humanTurn\":" + session.isHumanTurn() + ","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}";
    }

    private String turnMessage(GameSession session) {
        return session.isHumanTurn() ? "Player#1's turn" :  "Player#2's turn";
    }

    // =======================
    // HTTP / IO UTILITIES
    // =======================
    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    private int parseIntField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) throw new NumberFormatException("field not found: " + field);
        return Integer.parseInt(matcher.group(1));
    }

    private String extractGroup(Pattern p, String input, int group) {
        Matcher m = p.matcher(input);
        if (!m.matches()) throw new IllegalArgumentException("no match");
        return m.group(group);
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
