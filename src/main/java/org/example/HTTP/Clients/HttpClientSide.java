package org.example.HTTP.Clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Scanner;

public class HttpClientSide {
    private static int[][] localBoard = {{0,0,0},{0,0,0},{0,0,0}};
    private static final HttpClient httpClient = HttpClient.newHttpClient();

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
        String jsonPayload = String.format("{\"board\":%s,\"position\":%d}", Arrays.deepToString(board).replace(" ", ""), pos);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/game/turn"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
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
        if (start == -1) return null;
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