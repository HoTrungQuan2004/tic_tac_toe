package org.example;

import java.io.PrintStream;

public class Board {
    private final int[][] cells;
    private final int[] rowCounter;
    private final int[] colCounter;
    private final int[] diaCounter;
    private int totalMoves;
    private final PrintStream printer;

    public Board(PrintStream printer) {
        cells       = new int[][]{{0,0,0},{0,0,0},{0,0,0}};
        rowCounter  = new int[3];
        colCounter  = new int[3];
        diaCounter  = new int[2];
        totalMoves  = 0;
        this.printer = printer;
    }

    // Constructor for RESTful
    public Board(int[][] currentBoard, PrintStream printer) {
        cells       = new int[][]{{0,0,0},{0,0,0},{0,0,0}};
        rowCounter  = new int[3];
        colCounter  = new int[3];
        diaCounter  = new int[2];
        totalMoves  = 0;
        this.printer = printer;

        int pos = 1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int cellValue = currentBoard[i][j];
                if (cellValue != 0) {
                    this.cells[i][j] = cellValue;
                    this.totalMoves++;
                    updateCounters(i,j,pos,cellValue);
                }
                pos++;
            }
        }
    }

    public int[][] getCells() {
        return cells;
    }

    public boolean placeMove(int position, int playerValue) {
        int row = toRow(position);
        int col = toCol(position);
        if (cells[row][col] != 0) return false;

        cells[row][col] = playerValue;
        updateCounters(row, col, position, playerValue);
        totalMoves++;
        return true;
    }

    public boolean checkWin(int position, int playerValue) {
        int row = toRow(position);
        int col = toCol(position);
        int sign = (playerValue == 1) ? 1 : -1;
        return rowCounter[row] == 3 * sign
                || colCounter[col] == 3 * sign
                || diaCounter[0]   == 3 * sign
                || diaCounter[1]   == 3 * sign;
    }

    public boolean isFull() {
        return totalMoves == 9;
    }

    public int[] firstEmptyCell() {
        for (int i = 0; i <= 2; i++)
            for (int j = 0; j <= 2; j++)
                if (cells[i][j] == 0) return new int[]{i, j};
        return null;
    }

    public void printMessage(String message) {
        printer.println(message);
    }

    public void printBoard() {
        for (int[] row : cells) {
            printer.print("|");
            for (int cell : row) printer.print(cell + "|");
            printer.println();
        }
    }

    private int toRow(int pos) {
        return switch (pos) {
            case 1, 2, 3 -> 0;
            case 4, 5, 6 -> 1;
            default       -> 2;   // 7, 8, 9
        };
    }

    private int toCol(int pos) {
        return switch (pos) {
            case 1, 4, 7 -> 0;
            case 2, 5, 8 -> 1;
            default       -> 2;   // 3, 6, 9
        };
    }

    private void updateCounters(int row, int col, int pos, int playerValue) {
        int sign = (playerValue == 1) ? 1 : -1;
        rowCounter[row] += sign;
        colCounter[col] += sign;

        if (pos == 1 || pos == 9) diaCounter[0] += sign;
        if (pos == 3 || pos == 7) diaCounter[1] += sign;
        if (pos == 5) { diaCounter[0] += sign; diaCounter[1] += sign; }
    }
}