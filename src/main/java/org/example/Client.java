package org.example;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String ipAddress = "127.0.0.1";
    private static final int port = 8080;

    public static void main(String[] args) throws IOException {
        try (   Socket s = new Socket(ipAddress, port);
                Scanner keyboard = new Scanner(System.in);
                Scanner input = new Scanner(s.getInputStream());
                PrintStream output = new PrintStream(s.getOutputStream(), true)) {

            System.out.println("Connected to " + ipAddress + ":" + port);

            while(input.hasNextLine()) {
                String serverResponse = input.nextLine();
                System.out.println(serverResponse);

                if (serverResponse.endsWith(":")) {
                    if (keyboard.hasNextLine()) {
                        String userInput = keyboard.nextLine();
                        output.println(userInput);

                        if (userInput.equals("q")) break;
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("Can not connect to " + ipAddress + ":" + port);
        }
        System.out.println("End of the game");
    }
}
