package org.itxuexi.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 加密解密工具类
 */
public class AESUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    // 随机生成一个 128 位（16 字节）的 AES 密钥
    private static final SecretKey SECRET_KEY;

    static {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            // 使用安全的随机数生成器
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(128, secureRandom);
            SECRET_KEY = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    /**
     * AES 加密方法
     * @param plainText 明文
     * @return 加密后的 Base64 编码字符串
     * @throws Exception 如果加密过程中出现异常
     */
    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * AES 解密方法
     * @param encryptedText 加密后的 Base64 编码字符串
     * @return 解密后的明文
     * @throws Exception 如果解密过程中出现异常
     */
    public static String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            String originalText = "Hello, World!";
            // 加密
            String encryptedText = encrypt(originalText);
            System.out.println("加密后的文本: " + encryptedText);

            // 解密
            String decryptedText = decrypt(encryptedText);
            System.out.println("解密后的文本: " + decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
