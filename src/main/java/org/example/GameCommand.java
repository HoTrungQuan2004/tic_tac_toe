package org.example;

public interface GameCommand {
    boolean canHandle(String input, boolean hasNext);
    void execute();
}
