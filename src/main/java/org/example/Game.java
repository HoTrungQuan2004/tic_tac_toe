package org.example;

import java.io.PrintStream;

public class Game {
    private final Board board;
    private final Player player1;
    private final Player player2;
    private boolean player1Turn;
    private final PrintStream printer;
    private int winner = 0;

    public Game(Player player1, Player player2, boolean player1StartsFirst, PrintStream printer) {
        this.board = new Board(printer);
        this.player1 = player1;
        this.player2 = player2;
        this.player1Turn = player1StartsFirst;
        this.printer = printer;
    }

    public void run() {
        board.printMessage("Hello!");

        while (!isGameOver()) {
            board.printBoard();
            Player current = player1Turn ? player1 : player2;
            int move = current.takeTurn(board);

            if (checkForWinner(move, current)) {
                break;
            }

            player1Turn = !player1Turn;
        }

        displayFinalResult();
    }

    private boolean isGameOver() {
        return winner != 0 || board.isFull();
    }

    private boolean checkForWinner(int move, Player current) {
        if (board.checkWin(move, current.getId())) {
            winner = current.getId();
            return true;
        }
        return false;
    }

    private void displayFinalResult() {
        board.printBoard();
        if (winner == 0) {
            printer.println("Draw");
        } else {
            printer.println("Player" + winner + " won");
        }
    }
}