package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class gameTest {
    private final PrintStream originalOut = System.out;
    private PipedOutputStream outputStream;
    private BufferedReader scanner;

    @BeforeEach
    void setUp() {
        outputStream = new PipedOutputStream();
        try {
            PipedInputStream inputStream = new PipedInputStream(outputStream); // Connect in constructor
            scanner = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }


        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() { System.setOut(originalOut); }

    @Test
    @DisplayName("Test empty starting argument")
    void testEmptyStart() throws IOException {
        // Intent: Verify that game will ask to re-enter the CLI when the value is empty
        tic_tac_toe.main(new String[]{});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("Test invalid starting argument")
    void testInvalidStart() throws IOException {
        // Intend: Verify that game will ask to re-enter the CLI when the value is invalid
        tic_tac_toe.main(new String[]{"a"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("Test with extra starting argument")
    void testExtraStart() throws IOException {
        // Intent: Verify that game will ask to re-enter the CLI when the value is more than 1
        tic_tac_toe.main(new String[]{"1 2"});
        assertEquals("Please, input a valid option [1-2]", scanner.readLine());
    }

    @Test
    @DisplayName("Test Startup Message and Order")
    void testStartupMessageAndOrder() throws IOException {
        System.setIn(new ByteArrayInputStream("2\n5\n8".getBytes(StandardCharsets.UTF_8)));

        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[] {"1"});
            } catch (IOException ex) {}
        });
        game.start();

        assertEquals("Hello!", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        game.interrupt();
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

        for (int i = 0; i < 5; i++) scanner.readLine();

        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|0|0|", scanner.readLine());
        assertEquals("|0|1|0|", scanner.readLine());

        game.interrupt();
    }

    @Test
    @DisplayName("Test Human Non-Integer Input")
    void testHumanNonIntegerInput() throws IOException {
        System.setIn(new ByteArrayInputStream("a\n1\n3\n5\n7\n9".getBytes(StandardCharsets.UTF_8)));

        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[]{"1"});
            } catch (Exception e) {
                e.printStackTrace();}
        });
        game.start();

        for (int i = 0; i < 5; i++) scanner.readLine();
        assertEquals("Error: Please enter a valid number, not text!", scanner.readLine());
        assertEquals("Player1 turn", scanner.readLine());

        game.interrupt();
    }

    @Test
    @DisplayName("Test Human Out-of-Range Integer Input")
    void testHumanOutOfRangeIntegerInput() throws IOException {
        System.setIn(new ByteArrayInputStream("10\n7\n8\n9".getBytes(StandardCharsets.UTF_8)));

        Thread game = new Thread(() -> {
            try {
                tic_tac_toe.main(new String[]{"1"});
            } catch (Exception e) {e.printStackTrace();}
        });
        game.start();

        for (int i = 0; i < 5; i++) scanner.readLine();
        assertEquals("Invalid input, please try again (1-9)!", scanner.readLine());

        game.interrupt();
    }

}
