package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class gameTest {
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
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        assertEquals("Hello!", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-002: Test Startup Message and Order (Computer first)")
    void testComputerStartupMessageAndOrder() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"2"});
            } catch (Exception e) {}
        });
        game.start();

        assertEquals("Hello!", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());
    }

    @Test
    @DisplayName("TS-003: Test empty starting argument")
    void testEmptyStart() throws IOException {
        // Intent: Verify that game will ask to re-enter the CLI when the value is empty
        tictactoe.main(new String[]{});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("TS-004: Test invalid starting argument")
    void testInvalidStart() throws IOException {
        // Intend: Verify that game will ask to re-enter the CLI when the value is invalid
        tictactoe.main(new String[]{"3"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tictactoe.main(new String[]{"0"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tictactoe.main(new String[]{"-1"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tictactoe.main(new String[]{"abc"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("TS-005: Test with extra starting argument")
    void testExtraStart() throws IOException {
        // Intent: Verify that game will ask to re-enter the CLI when the value is more than 1
        tictactoe.main(new String[]{"01"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tictactoe.main(new String[]{"1 2"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        tictactoe.main(new String[]{"\" 1 \""});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("TS-006: Test board renders as 3x3 with state values only")
    void testBoardRendersAs3x3WithStateValues() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
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
                tictactoe.main(new String[] {"1"});
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
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("abc\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("@\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-009: Test quit game with q")
    void testQuitGame() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
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
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("Q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write(" q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("q \n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-011: Reject integer outside 1-9")
    void testRejectIntegerOutside1() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("0\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("10\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("-3\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-012: Reject move to occupied cell")
    void testRejectMoveToOccupiedCell() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
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
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-013: Human win condition")
    void testHumanWinCondition() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
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
    @DisplayName("TS-014: Computer win detection")
    void testComputerWinDetection() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);
        inputPipe.write("4\n7\n6\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(20, scanner);
        assertEquals("|2|2|2|", scanner.readLine());
        assertEquals("|1|0|1|", scanner.readLine());
        assertEquals("|1|0|0|", scanner.readLine());
        assertEquals("Player2 won", scanner.readLine());
    }

    @Test
    @DisplayName("TS-015: Draw condition after human move")
    void testDrawConditionAfterHumanMove() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("2\n4\n5\n7\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(32, scanner);
        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|2|", scanner.readLine());
        assertEquals("|1|2|1|", scanner.readLine());
        assertEquals("Draw", scanner.readLine());
    }

    @Test
    @DisplayName("TS-016: Draw condition after computer move")
    void testDrawConditionAfterComputerMove() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"2"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("2\n4\n7\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(32, scanner);
        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|2|2|", scanner.readLine());
        assertEquals("|1|2|1|", scanner.readLine());
        assertEquals("Draw", scanner.readLine());
    }

    @Test
    @DisplayName("TS-017: Computer chooses first available cell")
    void testComputerChoosesFirstAvailableCell() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"2"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        assertEquals("|2|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("2\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(4, scanner);
        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-018: Board integrity after every move")
    void testBoardIntegrityAfterEveryMove() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("2\n4\n5\n7\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();

        assertEquals("|0|1|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());

        assertEquals("|2|1|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        assertEquals("|2|1|0|", scanner.readLine());
        assertEquals("|1|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());

        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());

        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|2|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|2|", scanner.readLine());
        assertEquals("|1|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());

        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|2|", scanner.readLine());
        assertEquals("|1|2|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|2|", scanner.readLine());
        assertEquals("|1|2|1|", scanner.readLine());
        assertEquals("Draw", scanner.readLine());
    }

    @Test
    @DisplayName("TS-019: Turn prompt sequence correctness")
    void testTurnPromptSequenceCorrectness() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        skipLines(5, scanner);

        inputPipe.write("1\nabc\n3\n1\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("|1|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());

        skipLines(4, scanner);

        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        assertEquals("|1|2|1|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());

        skipLines(4, scanner);

        assertEquals("This cell is occupied, please choose another cell!", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        assertEquals("|1|2|1|", scanner.readLine());
        assertEquals("|2|0|0|", scanner.readLine());
        assertEquals("|0|0|1|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());

        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
    }

    @Test
    @DisplayName("TS-020: End states stop program")
    void testStopProgram() throws IOException, InterruptedException {
        Thread gameHumanWin = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        gameHumanWin.start();
        skipLines(5, scanner);
        inputPipe.write("7\n8\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(16, scanner);
        assertEquals("|2|2|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|1|1|1|", scanner.readLine());
        assertEquals("Player1 won", scanner.readLine());
        gameHumanWin.join(1000);
        tearDown();

        setUp();
        Thread gameComWin = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        gameComWin.start();
        skipLines(5, scanner);
        inputPipe.write("4\n7\n6\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(20, scanner);
        assertEquals("|2|2|2|", scanner.readLine());
        assertEquals("|1|0|1|", scanner.readLine());
        assertEquals("|1|0|0|", scanner.readLine());
        assertEquals("Player2 won", scanner.readLine());
        gameComWin.join(1000);
        tearDown();

        setUp();
        Thread gameDraw = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        gameDraw.start();
        skipLines(5, scanner);
        inputPipe.write("2\n4\n5\n7\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(32, scanner);
        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|2|", scanner.readLine());
        assertEquals("|1|2|1|", scanner.readLine());
        assertEquals("Draw", scanner.readLine());
        gameDraw.join(1000);
        tearDown();

        setUp();
        Thread gameQuit = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        gameQuit.start();
        skipLines(5, scanner);
        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("End of the game", scanner.readLine());
        gameQuit.join(1000);
    }

    @Test
    @DisplayName("TS-021: Fixed Thread Monitoring")
    void testInputLoopResilience() throws IOException, InterruptedException {
        final Throwable[] errorInThread = {null};

        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Throwable e) {
                if (!e.getClass().getSimpleName().contains("GameQuitException")) {
                    errorInThread[0] = e;
                }
            }
        });

        game.start();
        skipLines(5, scanner);

        String fakeInput = "akja\n".repeat(35) + "5\n" + "q\n";
        inputPipe.write(fakeInput.getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();

        boolean moveAccepted = false;
        String line;
        while ((line = scanner.readLine()) != null) {
            if (line.contains("|0|1|0|")) {
                moveAccepted = true;
            }
            if (line.contains("End of the game")) break;
        }

        game.join(2000);

        if (errorInThread[0] != null) {
            fail("Game thread failed with: " + errorInThread[0].getMessage());
        }

        assertTrue(moveAccepted, "Game should be responsive and accept valid move '5' after rapid invalid retries");
    }

    @Test
    @DisplayName("TS-022: Output consistency with exact required strings")
    void testOutputConsistency() throws IOException, InterruptedException {
        Thread invalidArg = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"abc\nq\n"});
            } catch (Exception e) {}
        });
        invalidArg.start();
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        invalidArg.join(1000);
        tearDown();

        setUp();
        Thread invalidMove = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        invalidMove.start();
        skipLines(5, scanner);
        inputPipe.write("abc\nq\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:",  scanner.readLine());
        invalidMove.join(1000);
        tearDown();

        setUp();
        Thread occupiedCell = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        occupiedCell.start();
        skipLines(5, scanner);
        inputPipe.write("1\n1\nq\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(8, scanner);
        assertEquals("This cell is occupied, please choose another cell!", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());
        occupiedCell.join(1000);
        tearDown();

        setUp();
        Thread quit = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        quit.start();
        skipLines(5, scanner);
        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("End of the game", scanner.readLine());
        quit.join(1000);
        tearDown();

        setUp();
        Thread Draw = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        Draw.start();
        skipLines(5, scanner);
        inputPipe.write("2\n4\n5\n7\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(32, scanner);
        assertEquals("|2|1|2|", scanner.readLine());
        assertEquals("|1|1|2|", scanner.readLine());
        assertEquals("|1|2|1|", scanner.readLine());
        assertEquals("Draw", scanner.readLine());
        Draw.join(1000);
        tearDown();

        setUp();
        Thread win = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        win.start();
        skipLines(5, scanner);
        inputPipe.write("7\n8\n9\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        skipLines(19, scanner);
        assertEquals("Player1 won", scanner.readLine());
        win.join(1000);
    }

    @Test
    @DisplayName("TS-024: Board display format")
    void testBoardDisplayFormat() throws IOException {
        Thread game = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        game.start();

        assertEquals("Hello!", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());

        inputPipe.write("5\nq\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|1|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());
    }

    @Test
    @DisplayName("TS-025: Startup argument contract")
    void testStartupArgumentContract() throws IOException, InterruptedException {
        Thread valid =  new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        valid.start();
        assertEquals("Hello!", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());
        inputPipe.write("q\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        valid.join(1000);
        tearDown();

        setUp();
        Thread Empty = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"\nq\n"});
            } catch (Exception e) {}
        });
        Empty.start();
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        Empty.join(1000);
        tearDown();

        setUp();
        Thread Unvalid = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"abc\nq\n"});
            } catch (Exception e) {}
        });
        Unvalid.start();
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
        Unvalid.join(1000);
    }

    @Test
    @DisplayName("TS-026: non-integer parsing normalization")
    void testNonIntegerParsingNormalization() throws IOException, InterruptedException {
        Thread startSpaceQuit = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        startSpaceQuit.start();
        skipLines(5, scanner);
        inputPipe.write(" q\nq\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());
        startSpaceQuit.join(1000);
        tearDown();

        setUp();
        Thread endSpaceQuit = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        endSpaceQuit.start();
        skipLines(5, scanner);
        inputPipe.write("q \nq\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("Please, input a valid number [1-9]", scanner.readLine());
        assertEquals("Player#1's turn:", scanner.readLine());
        startSpaceQuit.join(1000);
        tearDown();

        setUp();
        Thread inputWithSpaces = new Thread(() -> {
            try {
                tictactoe.main(new String[] {"1"});
            } catch (Exception e) {}
        });
        inputWithSpaces.start();
        skipLines(5, scanner);
        inputPipe.write(" 5 \nq\n".getBytes(StandardCharsets.UTF_8));
        inputPipe.flush();
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|1|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player#2's turn...", scanner.readLine());
        inputWithSpaces.join(1000);
    }
}
