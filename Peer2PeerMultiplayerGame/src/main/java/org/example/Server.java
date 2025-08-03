package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{
    private ExecutorService executorService = Executors.newCachedThreadPool();
    public static final int PORT = 7777;

    @Override
    public void run() {
        Thread.currentThread().setName("Server");

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Could not run server " + e.getMessage());
            return;
        }

        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket newPeerConnection = null;

            try {
                newPeerConnection = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Could not accept new connection " + e.getMessage());
                continue;
            }

            System.out.println("---- New connection: ----");
            System.out.println("-> Local IP: "      + newPeerConnection.getLocalAddress());
            System.out.println("-> Local PORT: "    + newPeerConnection.getLocalPort());
            System.out.println("-> IP: "            + newPeerConnection.getInetAddress());
            System.out.println("-> Port: "          + newPeerConnection.getPort());
            System.out.println("-------------------------");

            try {
                executorService.submit(new Peer(newPeerConnection));
            } catch (IOException e) {
                System.out.println("Could not run peer: " + e.getMessage());
            }
        }
    }


}
