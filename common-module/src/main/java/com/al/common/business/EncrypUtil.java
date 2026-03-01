package com.al.common.business;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import com.sun.org.apache.bcel.internal.generic.RETURN;

public class EncrypUtil {
    public static String encrypt(String plainText) {
        byte[] bytes = Const.ENCRYPT_PREFIX.getBytes();
        SM4 sm4 = SmUtil.sm4(bytes);
        return  sm4.encryptHex(plainText);
    }
    public static String decrypt(String encryptHex) {
        byte[] bytes = Const.ENCRYPT_PREFIX.getBytes();
        SM4 sm4 = SmUtil.sm4(bytes);
        return sm4.decryptStr(encryptHex);
    }
    public static String encrypt(String plainText, String key) {
        byte[] bytes = checkSm4Key(key);
        SM4 sm4 = SmUtil.sm4(bytes);
       return  sm4.encryptHex(plainText);
    }
    public static String decrypt(String encryptHex, String key) {
        byte[] bytes = checkSm4Key(key);
        SM4 sm4 = SmUtil.sm4(bytes);
        return sm4.decryptStr(encryptHex);
    }
    public static byte[] checkSm4Key(String key) {
        if (key == null || key.length() != 16) {
            throw new IllegalArgumentException("SM4 密钥必须是 16 字节（128 bit）");
        }
        return key.getBytes();
    }

    public static void main(String[] args) {
//        System.out.println(encrypt("6222020200088888888"));
        System.out.println(decrypt("efb4497ac5f77f2f69c99b87d1f947d38c217312568911e12f223b5fdc00070f"));
    }
}
