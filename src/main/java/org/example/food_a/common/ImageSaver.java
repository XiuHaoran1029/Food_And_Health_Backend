package org.example.food_a.common;

import org.springframework.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSaver {

    // 图片格式映射
    private static final Map<String, String> MIME_TO_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp",
            "image/svg+xml", "svg",
            "image/bmp", "bmp"
    );

    private static final Pattern BASE64_HEADER_PATTERN = Pattern.compile("^data:(image/\\w+[+\\w]*);base64,", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter MEAL_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ==================== 1. 保存用户头像（自动识别格式，返回文件名）====================
    public static String saveAvatar(String base64Image, Long userId, String targetDir) throws IOException {
        if (!StringUtils.hasText(base64Image)) {
            return "empty.jpg";
        }

        // 解析格式 + 纯base64
        Base64Info info = parseBase64(base64Image);
        byte[] data = Base64.getDecoder().decode(info.content.trim());

        // 创建目录
        Path dir = Paths.get(targetDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        // 文件名：userId.后缀
        String fileName = userId + "." + info.ext;
        Path filePath = dir.resolve(fileName);

        // 写入（覆盖）
        Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        // 返回 文件名 → 存入数据库
        return fileName;
    }

    // ==================== 2. 保存餐食图片（返回文件名）====================
    public static String saveMealImage(String base64Image, Long userId, Byte mealType, String targetDir) throws IOException {
        if (!StringUtils.hasText(base64Image)) {
            throw new IOException("图片不能为空");
        }

        Base64Info info = parseBase64(base64Image);
        byte[] data = Base64.getDecoder().decode(info.content.trim());

        Path dir = Paths.get(targetDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        // 文件名：userId_类型_时间戳.后缀
        String fileName = userId + "_" + mealType + "_" + LocalDateTime.now().format(MEAL_FORMATTER) + "." + info.ext;
        Path filePath = dir.resolve(fileName);

        Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        return fileName;
    }

    // ==================== 3. 本地图片 → Base64 ====================
    public static String fileToBase64(String imagePath) {
        if (!StringUtils.hasText(imagePath) || imagePath.startsWith("data:image")) {
            return imagePath;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            System.err.println("图片不存在：" + imagePath);
            return imagePath;
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mime = getMimeType(imagePath);
            return "data:" + mime + ";base64," + base64;
        } catch (Exception e) {
            e.printStackTrace();
            return imagePath;
        }
    }

    // ==================== 【核心通用】解析 Base64 ====================================
    private static Base64Info parseBase64(String base64) {
        String ext = "jpg";
        String content = base64;

        Matcher matcher = BASE64_HEADER_PATTERN.matcher(base64);
        if (matcher.find()) {
            String mime = matcher.group(1).toLowerCase();
            ext = MIME_TO_EXT.getOrDefault(mime, "jpg");
            content = base64.substring(matcher.end());
        }

        return new Base64Info(ext, content);
    }

    // ==================== 根据文件名获取 MIME ====================
    private static String getMimeType(String path) {
        String p = path.toLowerCase();
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".gif")) return "image/gif";
        if (p.endsWith(".webp")) return "image/webp";
        if (p.endsWith(".bmp")) return "image/bmp";
        if (p.endsWith(".svg")) return "image/svg+xml";
        return "image/jpeg";
    }

    // ==================== 内部存储对象 ====================
    private static class Base64Info {
        String ext;    // 图片后缀
        String content;// 纯base64内容

        Base64Info(String ext, String content) {
            this.ext = ext;
            this.content = content;
        }
    }
}