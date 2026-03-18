package org.example.food_a.common;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class DecryptRSA {

    // 你的原始私钥 (PKCS#1 格式)
    private static final String privateKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpQIBAAKCAQEA3XUVeWOxTNgtlLXLaQQqCHrtrEaaxmOJBEionbLnuqJAI96w\n" +
            "JRHH5eLNq9R8xRdmrH8GZuH5227EWKmnvnmmDwBVka1auhnIhtFPuAP1ty7lZtTB\n" +
            "/7miXzyz5E7cs1cC2CqBfojUmbarg6AkIxtPYpR/3W+5KJePz+RuPHWpHTdZq0Ig\n" +
            "/3Hrm3yg7srTsHYwEYON/NtnI83DKgx0GOMPrqqZXie89Z2OROcMt2I7RXXMgSVM\n" +
            "SllKMES7CwHUPu2s/7YNsIO8HgKoPdRC+LMFBnf3VLx0PWcFPr6EFto7AICB1855\n" +
            "vOcOmiXs0SpkeSLFm8ZiE5kerXmG/9JdJMX6UwIDAQABAoIBAB6Ck/vOXU3t9pGn\n" +
            "XifQuA3FCduTOp2cdvMNI5XeYQiYc+e9NV1WDslALv++XZY1vCKFuZcRCQBPSWPB\n" +
            "s+eNEPC+JgvU60v6Y2MXkTkT6pVbM6WwKC4YICOo1EvfBY77rNulN4P8S/YFcanH\n" +
            "tVLU32IAfYE/dC4Ae4KzwPA5cZwhk2fqbV+8CBiISMhTKExYbpxvFO8jP55HS5a7\n" +
            "tj5u185pxCmgHKO1KkjXwMDL1sg+PgZqnN2S6ej+9oOtBOVg/8gBFfJdN7YtCBXR\n" +
            "PW7nweUInajMQRXC+Hvp2pS+pxKo5Nbk0jlgJ0Z9ajf7XAHKVxKWz1rojH4h1f6i\n" +
            "mFTHy8ECgYEA9c9WZocDbMlnaGN89esnJY6zJ3EBblYFza0m1MQBC0yrZeJb2T5V\n" +
            "X8CFk3n8HY92aaPEhz3Y3lvZ5Qd3qiSPi/+oDQIusEmnbeLx2BjfLzXD+aygdDwD\n" +
            "3jkJjvdeoOQFsPvqOANMB1HhmjWQyyvHViX5ZcKjHG9oyWroHJvIdjMCgYEA5qNN\n" +
            "723t8oN6NKR7wJTGorbShEENA60tcDpCOWXrrcHB4qqK+1bUjtxUueXMpxuOwpgt\n" +
            "q2JN6R/llyMNnxraSFNqojIc2SbB+N0Z1zUUbeD2mdeTK94ARzwUGLBqyzW+SDP+\n" +
            "iWVi3vIq125GfwsASLtaquTo+7rzj8cyWt1PC2ECgYEAiK3uU/o03FOtDS1suW01\n" +
            "Cjwrv/2lFw38Eh56ngoSro7GK1PPUJ/JZwGtBJDqsCEqbZkjWTP5l8KpCSG61z+X\n" +
            "FfhlID6baWi7EW4Ene1OhHi37OehHKCWYgWDHBzOYFYKGBwrY1Pp/ZP5Bv9L8Lz6\n" +
            "DRv2bi/o9JG6SW0JfYbd02MCgYEAiD0lF28PZPcnZBtZgswffhSSzE8E2gwdUDMJ\n" +
            "1ZAQJObUmgl16bxD+VPLtLg7KOdNcEOmjpuWPER8QcgSDs807A5iagdCJUAQyuoI\n" +
            "jqirNbDy2rOOGxabgXjKwj7MBtfrXAlirVS3j8zrY7CfcgJUm6IYj+SCf9jScTcQ\n" +
            "1r3uKGECgYEAwUgKMkouBKN1hL4sKAXn7rDD57xC7kdVnq2Dk9KBkjqnn2DOlgtZ\n" +
            "zzItcoQmKJOiwD8LmI33dQWt/8duBmnrs4M2W3L+CD4mrPzyEr7x3PqRHck6yOF8\n" +
            "0U3atqXh7XLpY0XHOCYbC2shubpc5HZZHG6VPXvf8bWjS7HP34vDA9k=\n" +
            "-----END RSA PRIVATE KEY-----\n";

    public static String decryptRSA(String encryptedStr) throws Exception {
        if (encryptedStr == null || encryptedStr.trim().isEmpty()) {
            throw new IllegalArgumentException("加密字符串不能为空");
        }

        // 1. 解析私钥
        PrivateKey privateKey = parsePrivateKey(privateKeyPem);

        // 2. 初始化 Cipher (RSA/ECB/PKCS1Padding 是前端 jsencrypt 的默认配置)
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // 3. Base64 解码并解密
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedStr.trim());
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 解析私钥：自动兼容 PKCS#1 和 PKCS#8 格式
     */
    private static PrivateKey parsePrivateKey(String privateKeyPem) throws Exception {
        // 1. 清理空白字符和头尾标记
        String content = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        if (content.isEmpty()) {
            throw new IllegalArgumentException("无效的私钥格式");
        }

        byte[] keyBytes = Base64.getDecoder().decode(content);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        // 2. 判断格式并转换
        if (privateKeyPem.contains("-----BEGIN RSA PRIVATE KEY-----")) {
            // 是 PKCS#1 格式，需要手动转换为 PKCS#8
            keyBytes = convertPkcs1ToPkcs8(keyBytes);
        }
        // 如果是 PKCS#8 (-----BEGIN PRIVATE KEY-----)，则直接使用 keyBytes

        // 3. 生成 PrivateKey 对象
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 将 PKCS#1 (RSAPrivateKey) 字节数组转换为 PKCS#8 (PrivateKeyInfo) 字节数组
     * PKCS#8 结构: SEQUENCE { seqAlgId, OCTET STRING (PKCS#1 data) }
     */
    private static byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) {
        try {
            // PKCS#8 的固定头部 (AlgorithmIdentifier for RSA)
            // 对应 ASN.1:
            // SEQUENCE {
            //   SEQUENCE { OBJECT OID 1.2.840.113549.1.1.1, NULL }
            //   OCTET STRING (encapsulating PKCS#1)
            // }

            byte[] pkcs8Header = new byte[] {
                    0x30, (byte) 0x81, (byte) 0xd3, // SEQUENCE, length (calculated below)
                    0x02, 0x01, 0x00,               // INTEGER 0 (version)
                    0x30, 0x0d,                     // SEQUENCE (AlgorithmIdentifier)
                    0x06, 0x09,                     // OBJECT IDENTIFIER
                    0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01, // rsaEncryption OID
                    0x05, 0x00,                     // NULL
                    0x04, (byte) 0x81, (byte) 0x81  // OCTET STRING header
            };

            // 计算 PKCS#1 数据的长度
            int pkcs1Len = pkcs1Bytes.length;

            // 动态调整头部长度字节
            // 总长度 = 13 (固定头) + 3 (长度前缀) + pkcs1Len
            // 实际上上面的 header 数组已经预留了位置，我们需要修正长度字节
            // 这里的逻辑是构造一个新的字节数组

            // 重新构建更通用的头部
            // OID: 1.2.840.113549.1.1.1 (rsaEncryption)
            byte[] oid = new byte[]{0x06, 0x09, 0x2a, (byte)0x86, 0x48, (byte)0x86, (byte)0xf7, 0x0d, 0x01, 0x01, 0x01};
            byte[] nullParam = new byte[]{0x05, 0x00};

            // AlgorithmIdentifier Sequence
            byte[] algIdSeq = concatenate(oid, nullParam);
            algIdSeq = wrapSequence(algIdSeq);

            // Version Integer (0)
            byte[] version = new byte[]{0x02, 0x01, 0x00};

            // Octet String wrapping PKCS#1
            byte[] pkcs1Octet = wrapOctetString(pkcs1Bytes);

            // Final Sequence
            byte[] inner = concatenate(version, algIdSeq);
            inner = concatenate(inner, pkcs1Octet);

            return wrapSequence(inner);

        } catch (Exception e) {
            throw new RuntimeException("PKCS#1 转 PKCS#8 失败", e);
        }
    }

    // --- 辅助 ASN.1 构建方法 ---

    private static byte[] wrapSequence(byte[] data) {
        return wrapTag((byte) 0x30, data);
    }

    private static byte[] wrapOctetString(byte[] data) {
        return wrapTag((byte) 0x04, data);
    }

    private static byte[] wrapTag(byte tag, byte[] data) {
        byte[] lenBytes = encodeLength(data.length);
        byte[] result = new byte[1 + lenBytes.length + data.length];
        result[0] = tag;
        System.arraycopy(lenBytes, 0, result, 1, lenBytes.length);
        System.arraycopy(data, 0, result, 1 + lenBytes.length, data.length);
        return result;
    }

    private static byte[] encodeLength(int length) {
        if (length < 128) {
            return new byte[]{(byte) length};
        } else if (length < 256) {
            return new byte[]{(byte) 0x81, (byte) length};
        } else if (length < 65536) {
            return new byte[]{(byte) 0x82, (byte) (length >> 8), (byte) length};
        } else {
            throw new IllegalArgumentException("长度过大，不支持");
        }
    }

    private static byte[] concatenate(byte[]... arrays) {
        int totalLen = 0;
        for (byte[] arr : arrays) totalLen += arr.length;
        byte[] result = new byte[totalLen];
        int offset = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }
}