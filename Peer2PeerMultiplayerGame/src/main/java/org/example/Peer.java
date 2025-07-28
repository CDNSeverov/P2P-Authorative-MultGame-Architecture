package org.example;

import java.io.*;
import java.net.Socket;

// Class is used to represent players/peers that connect to the server (basically represents the individual)

public class Peer implements Runnable {
    private final Socket socket;
    private final BufferedReader reader;
    private final  BufferedWriter writer;
    private String playerName;
    private boolean inLobby = false;
    private GameSession currentGame = null;
    private static Peer selfPeer;

    public Peer (Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        if (selfPeer == null) {
            selfPeer = this;
        }
    }

    public static Peer getSelfPeer() {
        return selfPeer;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isInLobby() {
        return inLobby;
    }

    public void setInQueue(boolean inQueue) {
        this.inLobby = inQueue;
    }
    public GameSession getCurrentGame() {
        return currentGame;
    }

    public void sendMessage(Message message) {
        if (message.signature == null) {
            CryptoUtil.signMessage(message);
        }
        try {
            writer.write(message.toString() + "\n");
            writer.flush();
        } catch (IOException e) {
            System.out.println("<!>Error sending message to " + playerName + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            playerName = reader.readLine();
            System.out.println(playerName + " connected");

            while (true) {
                String rawMessage = reader.readLine();
                if (rawMessage == null) break;

                Message message = new Message(rawMessage);
                TaskQueue.queue.add(new Task(this, message));
            }
        } catch (IOException e) {
            System.out.println("Connection with " + playerName + " lost");
        } finally {
            if (currentGame != null) {
                currentGame.playerDisconnected(this);
            }
            PeerList.removePeer(this);
        }
    }

    public String getIp() {
        return socket.getInetAddress().toString().replace("/","");
    }

    public String getUsername() {
        return playerName != null ? playerName : Constants.USERNAME;
    }

    public void setCurrentGame(GameSession currentGame) {
        this.currentGame = currentGame;
    }

    public void setInLobby(boolean b) {
        inLobby = b;
    }
}
