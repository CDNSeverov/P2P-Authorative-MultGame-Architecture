package org.example;

import java.io.*;
import java.net.Socket;

public class Peer implements Runnable {
    Socket socket;
    BufferedReader reader;
    private BufferedWriter writer;
    private String playerName;
    private boolean inQueue = false;

    public Peer (Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public synchronized void sendMessage(String message) throws IOException {
        writer.write(message + "\n");
        writer.flush();
    }

    @Override
    public void run() {
        try {
            playerName = reader.readLine();
            if (playerName == null) {
                throw new IOException("Connection closed during handshake");
            }

            System.out.println("Player registered: " + playerName);
            PeerList.addPlayer(this);

            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                if ("JOIN_QUEUE".equals(message)) {
                    inQueue = true;
                    PeerList.addToQueue(this);
                    System.out.println(playerName + " joined queue");
                } else if ("LEAVE_QUEUE".equals(message)) {
                    inQueue = false;
                    PeerList.removeFromQueue(this);
                    System.out.println(playerName + " left queue");
                }
            }
        } catch (Exception e) {
            System.out.println("Error with player " + playerName + ": " + e.getMessage());
        } finally {
            inQueue = false;
            PeerList.removePlayer(this);
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            System.out.println("Player disconnected: " + playerName);
        }
    }
}
