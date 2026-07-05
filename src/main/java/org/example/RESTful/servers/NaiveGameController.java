package org.example.RESTful.servers;

import org.example.AIPlayer;
import org.example.Board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NaiveGameController {

    public static void handle(Socket socket) throws IOException {
        HttpRequest request = parseRequest(socket.getInputStream());
        OutputStream out = socket.getOutputStream();

        if (request == null)
            return;

        if (!"POST".equalsIgnoreCase(request.method()) || !"/api/game/turn".equals(request.path())) {
            sendResponse(out, 404, "{\"error\":\"Not found\"}");
            return;
        }

        try {
            processGameTurn(request.body(), out);
        } catch (Exception e) {
            sendResponse(out, 500, "{\"error\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }

    // ==========================
    // Game Logic
    // ==========================
    private static void processGameTurn(String requestBody, OutputStream out) throws IOException {
        int[][] currentCells = parseBoardFromJSON(requestBody);
        int Pos = parsePositionFromJSON(requestBody);

        PrintStream printer = new PrintStream(OutputStream.nullOutputStream());
        Board board = new Board(currentCells, printer);

        // check valid
        if (Pos < 1 || Pos > 9) {
            sendResponse(out, 400, buildErrorJson(requestBody, "INVALID_MOVE", "Please, input a valid number [1-9]"));
            return;
        }

        // check occupied
        if (!board.placeMove(Pos, 1)) {
            sendResponse(out, 400,
                    buildErrorJson(requestBody, "CELL_OCCUPIED", "This cell is occupied, please choose another cell!"));
            return;
        }

        // check human win
        if (board.checkWin(Pos, 1)) {
            sendResponse(out, 200, buildResponseJSON(board, "Player 1 won"));
            return;
        }

        // check draw after user turn and before ai turn
        if (board.isFull()) {
            sendResponse(out, 200, buildResponseJSON(board, "Draw"));
            return;
        }

        // ai turn
        AIPlayer aiPlayer = new AIPlayer(2, printer);
        int aiPos = aiPlayer.takeTurn(board);

        // check ai win / draw
        String responseJson;
        if (board.checkWin(aiPos, 2)) {
            responseJson = buildResponseJSON(board, "Player 2 won");
        } else if (board.isFull()) {
            responseJson = buildResponseJSON(board, "Draw");
        } else {
            responseJson = buildResponseJSON(board, "Continue");
        }

        sendResponse(out, 200, responseJson);
    }

    // ==========================
    // HTTP parsing/writing
    // ==========================
    private record HttpRequest(String method, String path, Map<String, String> headers, String body) {
    }

    private static HttpRequest parseRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank())
            return null;

        String[] parts = requestLine.split(" ");
        if (parts.length < 2)
            return null;
        String method = parts[0];
        String path = parts[1];

        Map<String, String> headers = new HashMap<>();
        String Line;
        while ((Line = reader.readLine()) != null && !Line.isEmpty()) {
            int colon = Line.indexOf(":");
            if (colon > 0) {
                headers.put(Line.substring(0, colon).trim().toLowerCase(), Line.substring(colon + 1).trim());
            }
        }

        int contentLength = headers.containsKey("content-length")
                ? Integer.parseInt(headers.get("content-length"))
                : 0;

        char[] bodyChars = new char[contentLength];
        if (contentLength > 0) {
            reader.read(bodyChars, 0, contentLength);
        }

        return new HttpRequest(method, path, headers, new String(bodyChars));
    }

    private static void sendResponse(OutputStream out, int StatusCode, String json) throws IOException {
        byte[] bodyBytes = json.getBytes(StandardCharsets.UTF_8);

        String response = "HTTP/1.1 " + StatusCode + " " + statusText(StatusCode) + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.flush();
    }

    private static String statusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    // ==========================
    // Json helpers
    // ==========================
    private static String buildErrorJson(String requestBody, String status, String errorMessage) {
        return "{\"board\":" + requestBody.split("\"board\":")[1].split(",\"position\"")[0] +
                ",\"status\":\"" + status + "\",\"errorMessage\":\"" + errorMessage + "\"}";
    }

    private static String buildResponseJSON(Board board, String status) {
        String boardJson = Arrays.deepToString(board.getCells()).replace(" ", "");
        return String.format("{\"board\":%s,\"status\":\"%s\",\"errorMessage\":null}", boardJson, status);
    }

    private static int[][] parseBoardFromJSON(String json) {
        int[][] cells = new int[3][3];
        String clean = json.substring(json.indexOf("[[") + 2, json.indexOf("]]"));

        String flatString = clean.replace("],[", ",");
        String[] tokens = flatString.split(",");

        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j] = Integer.parseInt(tokens[index++].trim());
            }
        }
        return cells;
    }

    private static int parsePositionFromJSON(String json) {
        String key = "\"position\":";
        int idx = json.indexOf(key);
        String sub = json.substring(idx + key.length()).replaceAll("[^0-9]", "");
        return Integer.parseInt(sub.trim());
    }
}
