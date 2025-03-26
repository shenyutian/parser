package org.syt.parser.util;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class MD5 {

    public static String toMd5(String md5Str) {
        String result = null;
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(md5Str.getBytes("utf-8"));
            result = toHexString(algorithm.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String toMd5(File f) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            byte[] buff = new byte[1024 * 32];
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();

            int len = in.read(buff);
            while (len > 0) {
                algorithm.update(buff, 0, len);
                len = in.read(buff);
            }
            return toHexString(algorithm.digest());
        } catch (Exception e) {
        } finally {
            CLOSE(in);
        }
        return null;
    }

    /**
     * byte[] -> hex string
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        int j = bytes.length;
        for (int aByte : bytes) {
            int b = aByte;
            if (b < 0) {
                b += 256;
            }
            if (b < 16) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(b));
        }
        return hexString.toString();
    }

    public static void CLOSE(Closeable c) {
        try {
            c.close();
        } catch (Exception e) {
        }
    }

    public static byte[] toSha1(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(bytes);
            return messageDigest.digest();
        } catch (Exception e) {
        }
        return null;
    }

    public static byte[] toSha256(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes);
            return messageDigest.digest();
        } catch (Exception e) {
        }
        return null;
    }

    public static void main(String[] args) {
        String md5 = toMd5("project.json");
        System.out.println("md5 = " + md5);

        String jksFilePath = "/Users/zhulei/Library/Containers/com.tencent.xinWeChat/Data/Library/Application Support/com.tencent.xinWeChat/2.0b4.0.9/3455dcd6ce3175fef2979b417aae4be7/Message/MessageTemp/357d95ec20749d338afdd5a8161b34c3/File/keystore/TicTacToe.jks";
        String keystorePassword = "TicTacToe";
        try {
            // 加载JKS文件
            FileInputStream fileInputStream = new FileInputStream(jksFilePath);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, keystorePassword.toCharArray());

            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                System.out.println("Alias: " + alias);

                // 获取证书
                Certificate certificate = keyStore.getCertificate(alias);
                System.out.println("Certificate: " + certificate);
                certificate.getEncoded(); // sign_data()
                certificate.getPublicKey().getEncoded(); // certificate.getEncoded()
            }
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
