package com.uchain.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class Crypto{

    public static byte[] randomBytes(int n){
        byte[] bytes = new byte[n];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public static byte[] hash256(byte []data) {
        return sha256(sha256(data));
    }

    public static byte[] hash160(byte []data) {
        return RIPEMD160(sha256(data));
    }

    public static byte[] RIPEMD160(byte []data) {
        RIPEMD160Digest messageDigest = new RIPEMD160Digest();
        messageDigest.update(data, 0, data.length);
        byte []out = new byte[messageDigest.getDigestSize()];
        Arrays.fill(out, (byte)0);
        messageDigest.doFinal(out, 0);
        return out;
    }

    public static byte[] sha256(byte []data) {
        byte []bytes = null;
        try {
            bytes =  MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static byte[] sign(byte []message, byte []privateKey) {
        BinaryData sig = Ecdsa.encodeSignature(Ecdsa.sign(new BinaryData(CryptoUtil.byteToList(sha256(message))), PrivateKey.apply(new BinaryData(CryptoUtil.byteToList(privateKey)))));
        return CryptoUtil.listTobyte(sig.getData());
    }

    public static byte[] sign(byte[] message, PrivateKey privateKey) {
        BinaryData sig = Ecdsa.encodeSignature(Ecdsa.sign(new BinaryData(CryptoUtil.byteToList(sha256(message))), privateKey));
        return CryptoUtil.listTobyte(sig.getData());
    }

    public static boolean verifySignature(byte[] message, byte[] signature, byte[] pubKey) {
        PublicKey publicKey = PublicKey.apply(new BinaryData(CryptoUtil.byteToList(pubKey)));

        return new Ecdsa().verifySignature(new BinaryData(CryptoUtil.byteToList(sha256(message))), new BinaryData(CryptoUtil.byteToList(signature)), publicKey);
    }

    public static boolean verifySignature(byte[] message, BinaryData signature) {
        List<PublicKey> publicKeys = recoverPublicKey(signature,message);

        return new Ecdsa().verifySignature(new BinaryData(CryptoUtil.byteToList(sha256(message))), signature, publicKeys.get(0))
                && new Ecdsa().verifySignature(new BinaryData(CryptoUtil.byteToList(sha256(message))), signature, publicKeys.get(1));
    }

    public static List<PublicKey> recoverPublicKey(BinaryData signature, byte[] message) {
        return new Ecdsa().recoverPublicKey(signature,
                sha256(message));
    }

    public static byte[] AesEncrypt(byte []data, byte []key, byte []iv) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher aes = null;
        byte []bytes = null;

        try {
            aes = Cipher.getInstance("AES/CBC/PKCS5PADDING", BouncyCastleProvider.PROVIDER_NAME);
            aes.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            bytes = aes.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static byte[] AesDecrypt(byte []data, byte []key, byte []iv) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher aes = null;
        byte []bytes = null;
        try {
            aes = Cipher.getInstance("AES/CBC/PKCS5PADDING", BouncyCastleProvider.PROVIDER_NAME);
            aes.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            bytes = aes.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }


}
