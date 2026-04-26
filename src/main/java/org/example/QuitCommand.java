package org.example;

class QuitCommand implements GameCommand {
    @Override
    public boolean canHandle(String input,  boolean hasNext) {
        return !hasNext || "q".equals(input);
    }

    @Override
    public void execute() {
        System.out.println("End of the game");
        throw new GameQuitException();
    }
}
