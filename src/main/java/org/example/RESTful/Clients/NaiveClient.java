package org.example.RESTful.Clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class NaiveClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final String PATH = "/api/game/turn";

    private static int[][] localBoard = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hello!");

        while (true) {
            printLocalBoard();
            System.out.print("Your turn! Enter position [1-9] or 'q' to quit: ");
            String input = scanner.nextLine().trim();

            if ("q".equals(input)) {
                System.out.println("End of the game");
                break;
            }

            try {
                int pos = Integer.parseInt(input);
                if (pos < 1 || pos > 9) {
                    System.out.println("Please, input a valid number [1-9]");
                    continue;
                }

                // send request to server
                String responseBody = sendMoveToServer(localBoard, pos);

                // parse server response status and updated board
                String status = parseJsonField(responseBody, "status");

                if ("INVALID_MOVE".equals(status) || "CELL_OCCUPIED".equals(status)) {
                    String error = parseJsonField(responseBody, "errorMessage");
                    System.out.println("Error: " + error);
                    continue;
                }

                // update localBoard
                localBoard = parseBoardFromResponse(responseBody);

                if ("Player 1 won".equals(status)) {
                    printLocalBoard();
                    System.out.println("Player 1 won");
                    break;
                } else if ("Player 2 won".equals(status)) {
                    printLocalBoard();
                    System.out.println("Player 2 won");
                    break;
                } else if ("Draw".equals(status)) {
                    printLocalBoard();
                    System.out.println("Draw");
                    break;
                }

            } catch (NumberFormatException e) {
                System.out.println("Please, input a valid number [1-9]");
            } catch (Exception e) {
                System.out.println("Network error: " + e.getMessage());
            }
        }
    }

    private static String sendMoveToServer(int[][] board, int pos) throws Exception {
        String jsonPayload = String.format("{\"board\":%s,\"position\":%d}",
                Arrays.deepToString(board).replace(" ", ""), pos);

        byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);

        try (Socket socket = new Socket(HOST, PORT)) {
            OutputStream out = socket.getOutputStream();

            String requestHeader = "POST " + PATH + " HTTP/1.1\r\n" +
                    "Host: " + HOST + "\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + payloadBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(requestHeader.getBytes(StandardCharsets.UTF_8));
            out.write(payloadBytes);
            out.flush();

            return readBodyFromResponse(socket.getInputStream());
        }
    }

    private static String readBodyFromResponse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String statusLine = reader.readLine();
        if (statusLine == null)
            throw new IOException("No response from server");

        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        char[] bodyChars = new char[contentLength];
        if (contentLength > 0) {
            reader.read(bodyChars, 0, contentLength);
        }

        return new String(bodyChars);
    }

    public static void printLocalBoard() {
        for (int[] row : localBoard) {
            System.out.print("|");
            for (int cel : row) {
                System.out.print(cel + "|");
            }
            System.out.println();
        }
    }

    private static String parseJsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start == -1)
            return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static int[][] parseBoardFromResponse(String json) {
        int[][] board = new int[3][3];
        String clean = json.substring(json.indexOf("[[") + 2, json.indexOf("]]"));

        String flatString = clean.replace("],[", ",");
        String[] tokens = flatString.split(",");

        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Integer.parseInt(tokens[index++].trim());
            }
        }
        return board;
    }
}
