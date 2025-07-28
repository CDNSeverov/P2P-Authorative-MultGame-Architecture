package org.example;

public class Constants {
    public static int PORT = 7776;
    public static String MY_IP = System.getenv("My_IP") != null ? System.getenv("MY_IP") : "localhost";
    public static String USERNAME = System.getenv("PEER_NAME") != null ? System.getenv("PEER_NAME") : "User-" + (int)(Math.random() * 1000);
}
