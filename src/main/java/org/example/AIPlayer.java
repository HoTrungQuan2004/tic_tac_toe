package org.example;

import java.io.PrintStream;

class AIPlayer extends Player {

    public AIPlayer(int id, PrintStream printer) {
        super(id, printer);
    }

    @Override
    public int takeTurn(Board board) {
        printer.println("Player#" + id + "'s turn");
        int[] empty = board.firstEmptyCell();
        int position = empty[0] * 3 + empty[1] + 1;
        board.placeMove(position, value);
        return position;
    }
}
