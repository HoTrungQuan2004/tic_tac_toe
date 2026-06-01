package org.example;

import java.io.PrintStream;
import java.util.Scanner;

public class HumanPlayer extends Player {
    private final Scanner scanner;

    public HumanPlayer(int id, Scanner scanner, PrintStream printer) {
        super(id, printer);
        this.scanner = scanner;
    }

    @Override
    public int takeTurn(Board board) {
        while (true) {
            printer.println("Player#" + id + "'s turn:");

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
