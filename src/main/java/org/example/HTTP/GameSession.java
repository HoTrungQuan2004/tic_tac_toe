package org.example.HTTP;

import org.example.AIPlayer;
import org.example.Board;

import java.io.PrintStream;

public class GameSession {
    public enum Status {ONGOING, HUMAN_WIN, AI_WIN, DRAW}

    private final String id;
    private final Board board;
    private final AIPlayer aiPlayer;
    private final int[] snapshot = new int[9];
    private Status status;
    private boolean humanTurn;

    // use to delete all println (of game logic) to client
    private static final PrintStream Silent = new PrintStream(System.out) {
        @Override
        public void println(String x) {}

        @Override
        public void print(String x) {}

        @Override
        public void println() {}
    };

    public GameSession(String id, boolean humanGoFirst) {
        this.id = id;
        this.humanTurn = humanGoFirst;
        this.board = new Board(Silent);
        this.aiPlayer = new AIPlayer(2, Silent);
        this.status = Status.ONGOING;

        if (!humanGoFirst) {
            AiMove();
        }
    }

    public String getId() {return id;}
    public Status getStatus() {return status;}
    public boolean isHumanTurn() {return humanTurn;}
    public int[] getSnapshot() {return snapshot;}

    public synchronized String humanMove(int pos) {
        if (pos < 1 || pos > 9) {return "Please, input a valid number [1-9]";}

        if (!board.placeMove(pos, 1)) {return "This cell is occupied, please choose another cell!";}

        snapshot[pos - 1] = 1;

        if (board.checkWin(pos, 1)) {
            status = Status.HUMAN_WIN;
            humanTurn = false;
            return null;
        }
        if (board.isFull()) {
            status = Status.DRAW;
            humanTurn = false;
            return null;
        }

        humanTurn = false;
        AiMove();
        return null;
    }

    private void AiMove() {
        int pos = aiPlayer.takeTurn(board);
        snapshot[pos - 1] = 2;

        if (board.checkWin(pos, 2)) {
            status = Status.AI_WIN;
        } else if (board.isFull()) {
            status = Status.DRAW;
        } else {
            humanTurn = true;
        }
    }
}
