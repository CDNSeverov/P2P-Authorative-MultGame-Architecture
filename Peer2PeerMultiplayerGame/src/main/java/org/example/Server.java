package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// This class is used to connect all peers on the p2p network

public class Server extends Thread {
    private ExecutorService executor = Executors.newCachedThreadPool();
    public static final int PORT = 7777;
    private volatile boolean running = true;
    public void shutdown() {
        running = false;
        executor.shutdown();
    }
    // Making server
    @Override
    public void run() {
        Thread.currentThread().setName("Server");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on PORT: " + PORT);

            while (running) {
                try {
                    Socket newConnection = serverSocket.accept();
                    System.out.println("New player goonected: " + newConnection.getInetAddress().getHostAddress());
                    executor.submit(new Peer(newConnection));
                } catch (IOException e) {
                    if (running) {
                        System.out.println("Error accepting connection: " + e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("Could not start server: " + e.getMessage());
        }
    }
}
