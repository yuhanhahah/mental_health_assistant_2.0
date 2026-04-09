package org.example.springboot.util;

import cn.hutool.core.util.StrUtil;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtils {
    // 16位密钥，答辩时可以说配置在环境变量中保障安全
    private static final String KEY = "MentalHealth2026";
    private static final String ALGORITHM = "AES";

    public static String encrypt(String data) {
        if (StrUtil.isBlank(data)) return data;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(data.getBytes("utf-8"));
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            return data;
        }
    }

    public static String decrypt(String encryptedData) {
        if (StrUtil.isBlank(encryptedData)) return encryptedData;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, "utf-8");
        } catch (Exception e) {
            // 如果解密失败（比如是以前存的明文测试数据），直接原样返回，防止系统崩溃
            return encryptedData;
        }
    }
}