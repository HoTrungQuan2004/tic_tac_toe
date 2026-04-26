package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class tic_tac_toe {
    public static void main(String[] args) throws IOException {
        PrintStream printer = System.out;
        Scanner keyboard = new Scanner(System.in);
        int start = 9;

        // Swaping between normal run and Black-box test

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
//                if ( input.equals("1") || input.equals("2")) {
//                    start = Integer.parseInt(input.trim());
//                    break;
//                }
//
//                System.out.println("Please, input a valid option [1-2]");
//            } catch (NumberFormatException e) {
//              System.out.println("not int Please, input a valid option [1-2]");
//            }
//        }

        // For Black-box test
        if (args.length > 0) {
            InputProcessor.process(args[0], true);

            try {
                if ((!args[0].equals("1")) && (!args[0].equals("2"))) {
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

        // to run game
        Player player1 = new HumanPlayer(1, keyboard, printer);
        Player player2 = new AIPlayer(2, printer);
        Game game = new Game(player1, player2, start == 1, printer);
        game.run();
    }
}