package com.al.common.util;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import com.al.common.business.Const;

/**
 * 国密4 加密解密
 */
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
}
