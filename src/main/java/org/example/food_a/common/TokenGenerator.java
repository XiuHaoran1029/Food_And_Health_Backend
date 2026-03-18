package org.example.food_a.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Locale;


public class TokenGenerator {

    private static final String SEPARATOR = "|#|";
    // 建议：生产环境不要依赖文件系统路径读取默认值，直接存一个最小的透明图片Base64字符串
    private static final String DEFAULT_IMG_BASE64_PATH = "src/main/resources/avatar";
    // 硬编码一个 1x1 透明像素的 Base64 作为终极兜底，防止递归爆炸
    private static final String HARDCODED_EMPTY_IMG = "image/jpeg;iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, DEFAULT_IMG_BASE64_PATH);
    }

    public static String generateToken(Long userId, String username, String imgPath) {
        String userid = userId.toString();

        // 1. 对 ID 和 Username 进行 URL 安全的 Base64 编码
        String encodedUserId = Base64.getUrlEncoder().encodeToString(userid.getBytes(StandardCharsets.UTF_8));
        String encodedUsername = Base64.getUrlEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8));

        // 2. 获取图片 Base64 (本身已是 Base64 格式)
        String imgBase64 = convertImageToBase64(imgPath);
        // 确保没有换行符或空格 (标准 Base64 编码器有时会每 76 个字符加换行)
        String cleanImgBase64 = imgBase64.replaceAll("\\s+", "");

        // 3. 直接拼接 (注意：这里没有对整体进行 Base64 编码)
        return encodedUserId + SEPARATOR + encodedUsername + SEPARATOR + cleanImgBase64;
    }

//    public static String[] parseToken(String token) {
//        if (token == null || token.isBlank()) {
//            throw new IllegalArgumentException("Token不能为空");
//        }
//
//        try {
//            // 【修正点 1】: 不要对整个 Token 解码！直接按分隔符拆分
//            // 之前的代码在这里对包含 "|#|" 的字符串做 Base64 解码，必挂
//            String[] parts = token.split("\\|\\#\\|"); // 转义分隔符
//
//            if (parts.length != 3) {
//                throw new IllegalArgumentException("Token格式错误：字段数量不符，期望3个，实际:" + parts.length);
//            }
//
//            // 4. 分别解码前两个字段
//            String userId = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
//            String username = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
//            String imgBase64 = parts[2]; // 图片部分保持原样
//
//            return new String[]{userId, username, imgBase64};
//        } catch (IllegalArgumentException e) {
//            // 区分是 Base64 解码错误还是分割错误
//            if (e.getMessage() != null && e.getMessage().contains("Illegal base64")) {
//                throw new IllegalArgumentException("Token解析失败：包含非法字符或格式损坏", e);
//            }
//            throw new IllegalArgumentException("Token解析失败：" + e.getMessage(), e);
//        }
//    }



    private static String convertImageToBase64(String imgPath) {
        if (imgPath == null || imgPath.isBlank()) {
            return HARDCODED_EMPTY_IMG; // 确保这个常量也是带前缀的，或者返回空串
        }

        try {
            byte[] imageBytes;
            String mimeType = "image/jpeg"; // 默认 MIME 类型

            // 1. 读取文件字节并尝试推断 MIME 类型
            if (Files.exists(Paths.get(imgPath))) {
                imageBytes = Files.readAllBytes(Paths.get(imgPath));
                mimeType = probeMimeType(imgPath, imageBytes);
            } else {
                // 尝试作为类路径资源读取
                String resourcePath = imgPath.startsWith("/") ? imgPath.substring(1) : imgPath;
                if (resourcePath.startsWith("src/main/resources/")) {
                    resourcePath = resourcePath.substring("src/main/resources/".length());
                }

                try (var inputStream = TokenGenerator.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (inputStream == null) {
                        throw new IOException("Resource not found: " + resourcePath);
                    }
                    imageBytes = inputStream.readAllBytes();
                    // 资源路径下也尝试推断类型
                    mimeType = probeMimeType(resourcePath, imageBytes);
                }
            }

            // 2. 执行 Base64 编码
            String base64Data = Base64.getEncoder().encodeToString(imageBytes);

            // 3. 【关键修改】拼接 Data URL 前缀
            // 格式：data:<mimeType>;base64,<data>
            String dataUrl = "data:" + mimeType + ";base64," + base64Data;

            return dataUrl;

        } catch (Exception e) {
            System.err.println("图片转 Base64 失败：" + imgPath + "，原因：" + e.getMessage());
            e.printStackTrace();
            return HARDCODED_EMPTY_IMG;
        }
    }

    /**
     * 辅助方法：根据文件名后缀和文件头简单推断 MIME 类型
     */
    private static String probeMimeType(String pathOrName, byte[] bytes) {
        String lowerName = pathOrName.toLowerCase(Locale.ROOT);

        // 优先根据扩展名判断
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".webp")) return "image/webp";
        if (lowerName.endsWith(".gif")) return "image/gif";
        if (lowerName.endsWith(".bmp")) return "image/bmp";
        if (lowerName.endsWith(".svg")) return "image/svg+xml";

        // 如果扩展名不可靠，可以根据文件头魔数判断 (可选优化)
        if (bytes != null && bytes.length >= 4) {
            if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) {
                return "image/png";
            }
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
                return "image/jpeg";
            }
            if (bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x49 && bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x46) {
                // 可能是 WebP (需要更多检查，这里简化处理)
                // 检查第 8-11 字节是否为 "WEBP"
                if (bytes.length >= 12 && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
                    return "image/webp";
                }
            }
        }

        // 默认返回 jpeg，或者根据业务需求返回 image/octet-stream
        return "image/jpeg";
    }
}