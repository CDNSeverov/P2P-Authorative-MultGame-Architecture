package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;

public class Peer implements Runnable{
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static Peer peer;
    private GameSession currentGame = null;
    private boolean inQueue = false;

    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

        reader = new BufferedReader(inputStreamReader);
        writer = new BufferedWriter(outputStreamWriter);

        this.peer = this;
    }

    public String getIp() {
        return socket.getInetAddress().toString().replace("/","");
    }
    public GameSession getCurrentGame() {
        return currentGame;
    }
    public static Peer getPeer() {
        return peer;
    }

    public boolean getInQueue() {
        return inQueue;
    }

    public void setCurrentGame(GameSession gameSession) {
        this.currentGame = gameSession;
    }
    public void setInQueue(boolean b) {
        this.inQueue = b;
    }

    public void sendMessage(Message message) {
        if (message.signature == null) {
            CryptoUtil.signMessage(message);
        }
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            System.out.println("Could not send message to peer");
        }
    }

    public String waitForMessage() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void run() {
        PeerList.addPeer(this);

        while (true) {
            String rawMessage = waitForMessage();

            if (rawMessage == null) {
                System.out.println("<!> Connection to peer lost");
                break;
            }

            Message message = null;
            try {
                message = new Message(rawMessage);
            } catch (IOException e) {
                System.out.println("<!> Protocol Violation");
            }

            TaskQueue.queue.add(new Task(this, message));
        }

        if (currentGame != null) {
            currentGame.playerDisconnected(this);
        }

        PeerList.removePeer(this);

        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
