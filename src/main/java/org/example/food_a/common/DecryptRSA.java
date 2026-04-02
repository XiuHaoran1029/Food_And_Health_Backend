package org.example.food_a.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j  // 自动生成 log 对象
@Component
public class DecryptRSA {

    // 这里必须换成【PKCS#8 格式的私钥】！！！
    // 以 -----BEGIN PRIVATE KEY----- 开头
    private static final String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
            "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAK7BgZGl+51RDd/R\n" +
            "n2XX7dXfJiUd3t+V/gpJ7VnoX05ZLewti+hh3XHlqFQdyYiYpHoNwoz8Y2LehRpp\n" +
            "unuRZk0ZJLUWttzjMlGpe4/UaC8tg2/dNDBCjwD3oAQzvaD+V5B83YHVlOu8yXMm\n" +
            "RpuSYjGV9CknyrX5pxqJeHlzxCrFAgMBAAECgYAJD5NuP9Lp9zfm8PF40FdOjYFm\n" +
            "z0OtCBPh6K9byAaZA1q0MjBc3iYjL6sooVq/sFpm0avSr4tfpvXzxqOaoJFTPBHE\n" +
            "W4ghlgQ3NKNVf3eExNLLWRfvADjr3PBDjbd9EVTFY6CWoii4+rFPQHF4VGNT6yD9\n" +
            "dmOk48bOri5eUj+jyQJBAN7qj7e1tmUa40mdcqeEzgqL1QbhocS/AmPrtziifkXX\n" +
            "D6aswXu0EU4XA9DKqB7ksPUN257bfSdjz/puaXSjFI8CQQDIsSYG0ls35Bvj0eYe\n" +
            "MipqRTDE5elWfd5+THKAOdCKQZ3jJnxW6c7f1WVoakN4o5nB1FPo+6G8cAICOmRU\n" +
            "Q71rAkEAhqu8oab1B0XkNHAbeVsBQ1vRGfQsT/l5jzYzndKFaNoRiYdq6LoTjlLX\n" +
            "KkeUo/1TbuitWt6NUMO7PTeBHEb9vwJBAK35QymuuPyedb7seju66SHukPRfCgO8\n" +
            "YV/DDY8yjyEK+L+s4STG0lsfNiVp2Mt/pw5r7cGOQQqFXnhn/gk+H7kCQERnyE+P\n" +
            "NzOGfgTlL0rICV/H50hAhGw8iHR2Xf5IGisW3ohOQvCo/icuUjuEzLnLKqjJuYri\n" +
            "xc8xaUoGrAtdLls=\n" +
            "-----END PRIVATE KEY-----\n";

    public static String decryptRSA(String encryptedStr) throws Exception {
        if (encryptedStr == null || encryptedStr.trim().isEmpty()) {
            log.error("RSA解密异常：密码不能为空");
            throw new IllegalArgumentException("密码不能为空");
        }

        try {
            // 1. 清理私钥格式
            String privateKeyContent = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            // 2. 生成私钥
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 3. 解密（和 JSEncrypt 完全匹配）
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedStr.trim());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA解密异常：{}", e.getMessage());
            throw new RuntimeException("RSA 解密失败：密钥不匹配或密码错误");
        }
    }
}