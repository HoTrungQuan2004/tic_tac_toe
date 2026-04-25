package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class gameTest {
    // Old
//    private final PrintStream originalOut = System.out;
//    private PipedOutputStream outputStream;
//    private BufferedReader scanner;
//
//    @BeforeEach
//    void setUp() {
//        outputStream = new PipedOutputStream();
//        try {
//            PipedInputStream inputStream = new PipedInputStream(outputStream);
//            scanner = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//        } catch (IOException ex) {
//            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        System.setOut(new PrintStream(outputStream));
//    }
//
//    @AfterEach
//    void tearDown() {
//        System.setOut(originalOut);
//    }

    // New
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    private PipedOutputStream outputStream;
    private BufferedReader scanner;
    private PipedOutputStream inputPipe;

    @BeforeEach
    void setUp() throws IOException {
        outputStream = new PipedOutputStream();
        PipedInputStream gameOutputIs = new PipedInputStream(outputStream);
        scanner = new BufferedReader(new InputStreamReader(gameOutputIs, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(outputStream));

        inputPipe = new PipedOutputStream();
        PipedInputStream gameInputIs = new PipedInputStream(inputPipe);
        System.setIn(gameInputIs);
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setIn(originalIn);
        outputStream.close();
        inputPipe.close();
    }

    void skipLines(int number, BufferedReader reader) throws IOException {
        for (int i = 0; i < number; i++) {
            reader.readLine();
        }
    }

    @Test
    @DisplayName("TS-001: Test Startup Message and Order (Human first)")
    void testHumanStartupMessageAndOrder() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        assertEquals("Hello!", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-002: Test Startup Message and Order (Computer first)")
    void testComputerStartupMessageAndOrder() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"2"});
            } catch (Exception e) {}
        });
        game.start();

        assertEquals("Hello!", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player2 turn", scanner.readLine());
    }

    @Test
    @DisplayName("TS-003: Test empty starting argument")
    void testEmptyStart() throws IOException {
        // Intent: Verify that game will ask to re-enter the CLI when the value is empty
        tic_tac_toe.main(new String[]{});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("TS-004: Test invalid starting argument")
    void testInvalidStart() throws IOException {
        // Intend: Verify that game will ask to re-enter the CLI when the value is invalid
        tic_tac_toe.main(new String[]{"3"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tic_tac_toe.main(new String[]{"0"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tic_tac_toe.main(new String[]{"-1"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tic_tac_toe.main(new String[]{"abc"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("TS-005: Test with extra starting argument")
    void testExtraStart() throws IOException {
        // Intent: Verify that game will ask to re-enter the CLI when the value is more than 1
        tic_tac_toe.main(new String[]{"01"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tic_tac_toe.main(new String[]{"1 2"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tic_tac_toe.main(new String[]{"\" 1 \""});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("TS-006: Test board renders as 3x3 with state values only")
    void testBoardRendersAs3x3WithStateValues() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(1, scanner);
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        skipLines(1, scanner);

        inputPipe.write("5\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|1|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-007: Test accept valid human move and update board")
    void testAcceptValidHumanMoveAndUpdateBoard() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("5\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();

        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|1|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-008: Test handle non-integer input as invalid")
    void testHandleNonIntegerInput() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("abc\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("@\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-009: Test quit game with q")
    void testQuitGame() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("End of the game", scanner.readLine());
    }

    @Test
    @DisplayName("TS-010: Verify q case sensitivity")
    void testVerifyQCase() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("Q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write(" q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("q \n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-011: Reject integer outside 1-9")
    void testRejectIntegerOutside1() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("0\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("10\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("-3\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-012: Reject move to occupied cell")
    void testRejectMoveToOccupiedCell() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("1\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(4, scanner);

        inputPipe.write("1\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(4, scanner);
        assertEquals("This cell is occupied, please choose another cell!", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-013: Human win detection on row/column/diagonal")
    void testHumanWinDetectionOnRowAndColumn() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("7\n8\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(16, scanner);
        assertEquals("|2|2|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|1|1|1|", scanner.readLine());
        assertEquals("Player1 won", scanner.readLine());
    }

    @Test
    @DisplayName("Test Initial Board Mapping")
    void testInitialBoardMapping() throws IOException {
        System.setIn(new ByteArrayInputStream("8\n2\n5".getBytes(StandardCharsets.UTF_8)));

        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[]{"1"});
            } catch (IOException ex) {}
        });
        game.start();

        skipLines(5, scanner);

        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|1|0|", scanner.readLine());

        game.interrupt();
    }
}
