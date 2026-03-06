package com.al.common.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

public class RsaUtil {

    private static final String RSA = "RSA";
    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String SIGN_ALGORITHM = "SHA256withRSA"; // RSA2

    /* ======================= 密钥处理 ======================= */

    public static PrivateKey loadPrivateKey(String privateKey) throws Exception {
        if (!privateKey.matches("^[A-Za-z0-9+/=]+$")) {
            throw new IllegalArgumentException("私钥不是合法的 Base64 字符串");
        }
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance(RSA);
        return factory.generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(String publicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance(RSA);
        return factory.generatePublic(spec);
    }

    /* ======================= 签名 / 验签 ======================= */

    public static String sign(String content, String privateKey) throws Exception {
        PrivateKey priKey = loadPrivateKey(privateKey);
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(priKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public static boolean verify(String content, String sign, String publicKey) throws Exception {
        PublicKey pubKey = loadPublicKey(publicKey);
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    /* ======================= 加密 / 解密（不常用） ======================= */

    public static String encrypt(String content, String publicKey) throws Exception {
        PublicKey pubKey = loadPublicKey(publicKey);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String content, String privateKey) throws Exception {
        PrivateKey priKey = loadPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(content));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /* ======================= 支付通道专用：参数拼接 ======================= */

    public static String buildSignContent(Map<String, Object> params) {
        // TreeMap 保证 ASCII 排序
        TreeMap<String, Object> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            if (entry.getValue() != null && !"".equals(entry.getValue())
                    && !"sign".equals(entry.getKey())) {
                sb.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }
        // 去掉最后一个 &
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // 密钥长度
        KeyPair pair = keyGen.generateKeyPair();

        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        // 转 Base64 保存
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        System.out.println(loadPrivateKey(privateKeyStr));
//        System.out.println("Private Key:\n" + privateKeyStr);
//        System.out.println("Public Key:\n" + publicKeyStr);
    }
}