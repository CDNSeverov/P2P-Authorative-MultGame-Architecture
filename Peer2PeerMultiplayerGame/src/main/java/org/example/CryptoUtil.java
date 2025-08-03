package org.example;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class CryptoUtil {
    public static PublicKey pub;
    public static PrivateKey pvt;

    public static void generatePubPvtKey() {
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
            g.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = g.generateKeyPair();
            pub = keyPair.getPublic();
            pvt = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public static boolean verifyMessage(Message message) {
        Signature ecdsaVerify = null;
        boolean valid = false;
        try {
            ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(message.sender));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(message.body.getBytes("UTF-8"));
            valid = ecdsaVerify.verify(Base64.getDecoder().decode(message.signature));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
            e.printStackTrace();
        }
        return valid;
    }

    public static Message signMessage(Message message) {
        try {
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(pvt);
            ecdsaSign.update(message.body.getBytes("UTF-8"));
            byte[] signature = ecdsaSign.sign();
            String pub_key = Base64.getEncoder().encodeToString(pub.getEncoded());
            String sig = Base64.getEncoder().encodeToString(signature);
            message.signature = sig;
            message.sender = pub_key;
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
            e.printStackTrace();
        }
        return message;
    }

    public static String getPubKey() {
        return Base64.getEncoder().encodeToString(pub.getEncoded());
    }

}