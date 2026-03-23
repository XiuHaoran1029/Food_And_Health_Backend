package org.example.food_a.common;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenGenerator {

    private static final String SEPARATOR = "|#|";

    // 只生成两部分：userId + username
    public static String generateToken(Long userId, String username) {
        // 1. 转字符串
        String userIdStr = userId.toString();

        // 2. URL 安全 Base64 编码
        String encodedUserId = Base64.getUrlEncoder()
                .encodeToString(userIdStr.getBytes(StandardCharsets.UTF_8));

        String encodedUsername = Base64.getUrlEncoder()
                .encodeToString(username.getBytes(StandardCharsets.UTF_8));

        // 3. 只拼接两部分
        return encodedUserId + SEPARATOR + encodedUsername;
    }

    // 解析：只解析前两部分
    public static String[] parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token 不能为空");
        }

        // 分割
        String[] parts = token.split("\\|\\#\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Token 格式错误，必须只有两段");
        }

        try {
            // 解码
            String userId = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String username = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            return new String[]{userId, username};
        } catch (Exception e) {
            throw new IllegalArgumentException("Token 解析失败：格式非法或已损坏", e);
        }
    }
}