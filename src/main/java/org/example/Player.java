package org.example;

import java.io.PrintStream;

public abstract class Player {
    protected final int id;
    protected final int value;
    protected final PrintStream printer;

    public Player(int id, PrintStream printer) {
        this.id    = id;
        this.value = id;
        this.printer = printer;
    }

    public int getId() { return id; }

    public abstract int takeTurn(Board board);
}

