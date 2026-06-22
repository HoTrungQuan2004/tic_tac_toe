package org.example.HTTP_Web_app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.AIPlayer;
import org.example.Board;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        new HTTPServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", this::dispatch);

        server.setExecutor(null);
        server.start();

        System.out.println("HTTP Server started. Listening on port " + PORT);
        System.out.println("POST /game/move");
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

            if ("POST".equalsIgnoreCase(method) && "/game/move".equals(path)) {
                handleMove(exchange);
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
    private void handleMove(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);

        int pos = parseIntField(body, "position");
        int[] currentBoard = parseBoardField(body);

        int[][] board2D = new int[3][3];
        for (int i = 0; i < 9; i++) {
            board2D[i / 3][i % 3] = currentBoard[i];
        }

        PrintStream printer = new PrintStream(OutputStream.nullOutputStream());
        Board board = new Board(board2D, printer);

        // check valid move
        if (pos < 1 || pos > 9) {
            sendJson(exchange, 400, "{\"error\":\"Please, input a valid number [1-9]\"}");
            return;
        }

        if (!board.placeMove(pos, 1)) {
            sendJson(exchange, 400, "{\"error\":\"This cell is occupied, please choose another cell!\"}");
            return;
        }

        // check human winning/draw
        if (board.checkWin(pos, 1)) {
            sendJson(exchange, 200, buildStateJson(board, "HUMAN_WIN", "Player 1 won"));
            return;
        }

        if (board.isFull()) {
            sendJson(exchange, 200, buildStateJson(board, "DRAW", "Draw"));
            return;
        }

        // Ai turn
        AIPlayer aiPlayer = new AIPlayer(2, printer);
        int aiPos = aiPlayer.takeTurn(board);

        // check winning/draw after ai turn
        if (board.checkWin(aiPos, 2)) {
            sendJson(exchange, 200, buildStateJson(board, "AI_WIN", "Player 2 win"));
        } else if (board.isFull()) {
            sendJson(exchange, 200, buildStateJson(board, "DRAW", "Draw"));
        } else {
            sendJson(exchange, 200, buildStateJson(board, "CONTINUE", "Player#1's turn"));
        }
    }

    // ===========================
    // JSON
    // ===========================
    private String buildStateJson(Board board, String status, String message) {
        int[] flatBoard = new int[9];
        int[][] board2D = board.getCells();

        for (int i = 0; i < 9; i++) {
            flatBoard[i] = board2D[i / 3][i % 3];
        }

        return "{"
                + "\"board\":" + Arrays.toString(flatBoard) + ","
                + "\"status\":\"" + status + "\","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}";
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

    private int[] parseBoardField(String json) {
        String[] token = json.substring(json.indexOf("[") + 1, json.indexOf("]")).split(",");
        int[] board = new int[token.length];
        for (int i = 0; i < token.length; i++) {
            board[i] = Integer.parseInt(token[i]);
        }
        return board;
    }

    private int parseIntField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) throw new NumberFormatException("field not found: " + field);
        return Integer.parseInt(matcher.group(1));
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
