package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;

public class Peer implements Runnable{
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private GameSession currentGame = null;
    private boolean inGame = false;
    private boolean inQueue = false;
    private boolean connected = true;
    private static Peer self;
    private String playerRole;
    private String remoteUsername;

    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

        reader = new BufferedReader(inputStreamReader);
        writer = new BufferedWriter(outputStreamWriter);

        self = this;
    }

    public String getIp() {
        return socket.getInetAddress().toString().replace("/","");
    }
    public GameSession getCurrentGame() {
        return currentGame;
    }

    public boolean getInQueue() {
        return inQueue;
    }
    public String getPlayerRole() {
        return playerRole;
    }
    public String getUsername() {
        return remoteUsername != null ? remoteUsername : Constants.USERNAME;
    }
    public boolean getInGame() {
        return inGame;
    }
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
    public void setRemoteUsername(String username) {
        this.remoteUsername = username;
    }
    public void setCurrentGame(GameSession gameSession) {
        this.currentGame = gameSession;
    }
    public void setInQueue(boolean b) {
        this.inQueue = b;
    }
    public void setPlayerRole(String role) {
        this.playerRole = role;
    }
    public static Peer getSelf() {
        return self;
    }

    public void sendMessage(Message message) {
        if (message.signature == null) {
            CryptoUtil.signMessage(message);
        }
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            System.out.println("<!> Could not send message to peer");
        }
    }

    public String waitForMessage() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public void sendPlayRequest() {
        this.sendMessage(new Message(MessageType.PLAY_REQUEST, ""));
    }

    public static void sendPlayRequestStatic() {
        if (self != null) {
            self.sendPlayRequest();
        } else {
            System.out.println("<!> Peer does not exist");
        }
    }

    @Override
    public void run() {
        PeerList.addPeer(this);

        while (connected) {
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