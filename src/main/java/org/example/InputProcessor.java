package org.example;

import java.util.ArrayList;
import java.util.List;

public class InputProcessor {
    private static List<GameCommand> Commands = new ArrayList<>();

    static {
        Commands.add(new QuitCommand() {});
    }

    public static boolean process(String input, boolean hasNext) {
        for (GameCommand command : Commands) {
            if (command.canHandle(input, hasNext)) {
                command.execute();
                return true;
            }
        }
        return false;
    }
}
