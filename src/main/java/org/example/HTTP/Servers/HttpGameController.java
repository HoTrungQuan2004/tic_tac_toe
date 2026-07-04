package org.example.HTTP.Servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.AIPlayer;
import org.example.Board;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpGameController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); //not allowed method
            return;
        }

        // read Request Body
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        // {"board":[[0,0,0],[0,0,0],[0,0,0]], "position": 5}
        int[][] currentCells =parseBoardFromJSON(requestBody);
        int humanPos = parsePositionFromJSON(requestBody);

        // Init Board
        PrintStream printer = new PrintStream(OutputStream.nullOutputStream());
        Board board = new Board(currentCells, printer);

        String responseJson;

        // check valid
        if (humanPos < 1 || humanPos > 9) {
            responseJson = "{\"board\":" + requestBody.split("\"board\":")[1].split(",\"position\"")[0] +
                    ",\"status\":\"INVALID_MOVE\",\"errorMessage\":\"Please, input a valid number [1-9]\"}";
            sendResponse(exchange, 400, responseJson);
            return;
        }

        // check occupied
        if (!board.placeMove(humanPos, 1)) {
            responseJson = "{\"board\":" + requestBody.split("\"board\":")[1].split(",\"position\"")[0] +
                    ",\"status\":\"CELL_OCCUPIED\",\"errorMessage\":\"This cell is occupied, please choose another cell!\"}";
            sendResponse(exchange, 400, responseJson);
            return;
        }

        // check human win
        if (board.checkWin(humanPos, 1)) {
            responseJson = buildResponseJSON(board, "Player 1 won");
            sendResponse(exchange, 200, responseJson);
            return;
        }

        // check draw after user turn and before ai turn
        if (board.isFull()) {
            responseJson = buildResponseJSON(board, "Draw");
            sendResponse(exchange, 200, responseJson);
            return;
        }

        // ai turn
        AIPlayer aiPlayer = new AIPlayer(2, printer);
        int aiPos = aiPlayer.takeTurn(board);

        // check ai win
        if (board.checkWin(aiPos, 2)) {
            responseJson = buildResponseJSON(board, "Player 2 won");
        } else if (board.isFull()) {
            responseJson = buildResponseJSON(board, "Draw");
        } else {
            responseJson = buildResponseJSON(board, "Continue");
        }

        sendResponse(exchange, 200, responseJson);
    }

    private void sendResponse(HttpExchange exchange, int status, String responseJson) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String buildResponseJSON(Board board, String status) {
        String boardJson = Arrays.deepToString(board.getCells()).replace(" ", "");
        return String.format("{\"board\":%s,\"status\":\"%s\",\"errorMessage\":null}", boardJson, status);
    }

    private int[][] parseBoardFromJSON(String json) {
        int[][] cells = new int[3][3];
        String clean = json.substring(json.indexOf("[[") + 2 , json.indexOf("]]"));

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

    private int parsePositionFromJSON(String json) {
        String key = "\"position\":";
        int idx = json.indexOf(key);
        String sub = json.substring(idx + key.length()).replaceAll("[^0-9]", "");
        return Integer.parseInt(sub.trim());
    }
}