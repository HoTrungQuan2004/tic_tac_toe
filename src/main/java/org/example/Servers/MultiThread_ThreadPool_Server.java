package org.example.Servers;

import org.example.AIPlayer;
import org.example.Game;
import org.example.HumanPlayer;
import org.example.Player;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThread_ThreadPool_Server {
    public static void main(String[] args) {
        int port = 8080;
        int maxThreads = 80;

        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);

        try (ServerSocket ss = new ServerSocket(port);) {
            System.out.println("Waiting for connection from port " + port);
            while (true) {
                Socket con = ss.accept();
                System.out.println("Accepted connection from " + con.getRemoteSocketAddress());

                threadPool.execute(() -> {
                    try (Scanner input = new Scanner(con.getInputStream());
                         PrintStream output = new PrintStream(con.getOutputStream(), true)) {

                        output.println("Please enter who go first:");
                        int start = Integer.parseInt(input.nextLine());
                        Player Player1 = new HumanPlayer(1, input, output);
                        Player Player2 = new AIPlayer(2, output);

                        Game game = new Game(Player1, Player2, start == 1, output);
                        game.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            con.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}
