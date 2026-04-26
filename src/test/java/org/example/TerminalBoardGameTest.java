package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class BoardTest {
    private Board board;
    private PipedOutputStream outputStream;
    private PrintStream printer;
    private BufferedReader scanner;

    @BeforeEach
    void setup() {
        outputStream = new PipedOutputStream();
        try{
            PipedInputStream inputStream = new PipedInputStream(outputStream);
            printer = new PrintStream(outputStream, true,  StandardCharsets.UTF_8);
            scanner = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, e);
        }
        board = new Board(printer);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (printer != null) printer.close();
        if (outputStream != null) outputStream.close();
        if (scanner != null) scanner.close();
    }

    @Test
    @DisplayName("Test win on row")
    void winOnRowTest() {
        // Intent: Verify that a player wins when they occupy all three cells in the top row.
        board.placeMove(1, 1);
        board.placeMove(2, 1);
        board.placeMove(3, 1);

        assertTrue(board.checkWin(3, 1), "Player1 should win");
    }

    @Test
    @DisplayName("Test win on column")
    void winOnColumnTest() {
        // Intent: Verify that a player wins when they occupy all three cells in the first column.
        board.placeMove(1, 1);
        board.placeMove(4, 1);
        board.placeMove(7, 1);

        assertTrue(board.checkWin(7, 1), "Player1 should win");
    }

    @Test
    @DisplayName("Test win on diagonal")
    void winOnDiagonalTest() {
        // Intent: Verify that a player wins when they occupy all three cells in a diagonal.
        board.placeMove(1, 1);
        board.placeMove(5, 1);
        board.placeMove(9, 1);

        assertTrue(board.checkWin(9, 1), "Player1 should win");
    }

    @Test
    @DisplayName("Test draw")
    void DrawTest() {
        // Intent: Verify that the game is draw when player 1 occupy all even cells, and player 2 occypy all odd cells
        int[] moves = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        for (int i = 0; i < moves.length; i++) {
            int PlayerTurn = (i % 2 == 0) ? 1 : 2;
            board.placeMove(moves[i], PlayerTurn);
        }

        assertTrue(board.isFull(), "Game should be draw");
    }

    @Test
    @DisplayName("Test Player2 choose first empty cell")
    void AIPlayerTest() {
        // Intent: Verify that player 2 can choose correct the first empty cell
        AIPlayer ai = new AIPlayer(2, printer);

        board.placeMove(1, 1);

        int AIMove = ai.takeTurn(board);

        assertEquals(2, AIMove, "AI should choose the 2st cell (the first empty one)");
    }
}
