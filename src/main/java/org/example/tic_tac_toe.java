package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

interface GameCommand {
    boolean canHandle(String input, boolean hasNext);
    void execute();
}

class GameQuitException extends RuntimeException {}

class QuitCommand implements GameCommand {
    @Override
    public boolean canHandle(String input,  boolean hasNext) {
        return !hasNext || "q".equals(input);
    }

    @Override
    public void execute() {
        System.out.println("End of the game");
        throw new GameQuitException();
    }
}

class Board {
    private final int[][] cells;
    private final int[] rowCounter;
    private final int[] colCounter;
    private final int[] diaCounter;
    private int totalMoves;
    private PrintStream printer;

    public Board() {
        cells       = new int[][]{{0,0,0},{0,0,0},{0,0,0}};
        rowCounter  = new int[3];
        colCounter  = new int[3];
        diaCounter  = new int[2];
        totalMoves  = 0;
        printer = new PrintStream(System.out);
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

abstract class Player {
    protected final int id;
    protected final int value;

    public Player(int id) {
        this.id    = id;
        this.value = id;
    }

    public int getId() { return id; }

    public abstract int takeTurn(Board board);
}

class HumanPlayer extends Player {
    private final Scanner scanner;
    private PrintStream printer;

    public HumanPlayer(int id, Scanner scanner) {
        super(id);
        this.scanner = scanner;
        printer = new PrintStream(System.out);
    }

    @Override
    public int takeTurn(Board board) {
        while (true) {
            printer.println("Player" + id + " turn");

            boolean hasNext = scanner.hasNextLine();
            String choiceStr = hasNext ? scanner.nextLine() : null;

            if (InputProcessor.process(choiceStr, hasNext)) {
                continue;
            }

            try {
                int choice = Integer.parseInt(choiceStr.trim());

                if (choice < 1 || choice > 9) {
                    printer.println("Please, input a valid number [1-9]");
                    continue;
                }

                if (board.placeMove(choice, value)) {
                    return choice;
                } else {
                    printer.println("This cell is occupied, please choose another cell!");
                }

            } catch (NumberFormatException e) {
                printer.println("Please, input a valid number [1-9]");
            }
        }
    }
}

class AIPlayer extends Player {
    private PrintStream printer;

    public AIPlayer(int id) {
        super(id);
        printer = new PrintStream(System.out);
    }

    @Override
    public int takeTurn(Board board) {
        printer.println("Player" + id + " turn");
        int[] empty = board.firstEmptyCell();
        int position = empty[0] * 3 + empty[1] + 1;
        board.placeMove(position, value);
        return position;
    }
}


class  Game {
    private final Board  board;
    private final Player player1;
    private final Player player2;
    private boolean      player1Turn;
    private PrintStream printer;

    public Game(Player player1, Player player2, boolean player1StartsFirst) {
        this.board        = new Board();
        this.player1      = player1;
        this.player2      = player2;
        this.player1Turn  = player1StartsFirst;
        printer = new PrintStream(System.out);
    }

    public void run() {
        int winner = 0;
        board.printMessage("Hello!");

        outer:
        while (true) {
            board.printBoard();
            Player current = player1Turn ? player1 : player2;
            int move = current.takeTurn(board);

            if (board.checkWin(move, current.getId())) {
                winner = current.getId();
                break;
            }
            if (board.isFull()) break;

            player1Turn = !player1Turn;
        }

        if (winner == 0) {
            printer.println("Draw");
        } else {
            board.printBoard();
            printer.println("Player" + winner + " won");
        }
    }
}


public class tic_tac_toe {
    public static void main(String[] args) throws IOException {
        Scanner keyboard = new Scanner(System.in);
        int start = 9;


        // Normal run
//        while (true) {
//            try {
//                boolean hasNext = keyboard.hasNextLine();
//                String input = hasNext ? keyboard.nextLine() : null;
//
//                if (InputProcessor.process(input, hasNext)) {
//                    continue;
//                }
//
//                if ( input == "1" || input == "2") {
//                    start = Integer.parseInt(input.trim());
//                    break;
//                }
//
//                System.out.println("Please, input a valid option [1-2]");
//            } catch (NumberFormatException e) {
//              System.out.println("Please, input a valid option [1-2]");
//            }
//        }

        // For Black-box test
        if (args.length > 0) {
            InputProcessor.process(args[0], true);

            try {
                if (args[0] != "1" && args[0] != "2") {
                    System.out.println("Please, input a valid option [1-2]");
                    return;
                }
                else start = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException e) {
                System.out.println("Please, input a valid option [1-2]");
                return;
            }
        }
        else {
            System.out.println("Please, input a valid option [1-2]");
            return;
        }

        Player player1 = new HumanPlayer(1, keyboard);
        Player player2 = new AIPlayer(2);
        Game game = new Game(player1, player2, start == 1);
        game.run();
    }
}