package org.example;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CryptoUtil.generatePubPvtKey();

        new Server().start();

        new InputScanner().start();

        new ProtocolHandler().start();

        if (!Constants.MY_IP.equals(Constants.BOOTSTRAP_IP)) {
            PeerList.connectToRemote(Constants.BOOTSTRAP_IP, Constants.PORT);
        }
    }
}