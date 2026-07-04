package org.example.HTTP_Web_app;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.AIPlayer;
import org.example.Board;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/game/move")
public class GameServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        String body = readBody(request);

        int pos;
        int[] FlatBoard;
        try {
            pos = parseIntField(body, "position");
            FlatBoard = parseBoardIntField(body);
        } catch (Exception e) {
            sendJson(response, 400, "{\"error\":\"Malformed request body\"}");
            return;
        }

        int[][] board2D = new int[3][3];
        for (int i = 0; i < 9; i++) {
            board2D[i / 3][i % 3] = FlatBoard[i];
        }

        PrintStream printer = new PrintStream(OutputStream.nullOutputStream());
        Board board = new Board(board2D, printer);

        if (pos < 1 || pos > 9) {
            sendJson(response, 400, "{\"error\":\"Please, input a valid number [1-9]\"}");
            return;
        }

        if (!board.placeMove(pos, 1)) {
            sendJson(response, 400, "{\"error\":\"This cell is occupied, please choose another cell!\"}");
            return;
        }

        if (board.checkWin(pos, 1)) {
            sendJson(response, 200, buildStateJson(board, "HUMAN_WIN", "Player 1 won"));
            return;
        }

        if (board.isFull()) {
            sendJson(response, 200, buildStateJson(board, "DRAW", "Draw"));
            return;
        }

        AIPlayer aiPlayer = new AIPlayer(2, printer);
        int aiPos = aiPlayer.takeTurn(board);

        if (board.checkWin(aiPos, 2)) {
            sendJson(response, 200, buildStateJson(board, "AI_WIN", "Player 2 won"));
        } else if (board.isFull()) {
            sendJson(response, 200, buildStateJson(board, "DRAW", "Draw"));
        } else {
            sendJson(response, 200, buildStateJson(board, "CONTINUE", "Player#1's turn"));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

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

    private void sendJson(HttpServletResponse response, int statusCode, String json) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(json);
    }

    private String readBody(HttpServletRequest request) throws IOException {
        return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    private int[] parseBoardIntField(String json) {
        String[] tokens = json.substring(json.indexOf("[") + 1, json.indexOf("]")).split(",");
        int[] board = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++)
            board[i] = Integer.parseInt(tokens[i].trim());
        return board;
    }

    private int parseIntField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find())
            throw new NumberFormatException("field not found: " + field);
        return Integer.parseInt(matcher.group(1));
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
