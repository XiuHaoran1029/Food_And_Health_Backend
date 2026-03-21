package org.example.food_a.common;

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

    // 常见的 MIME Type 到文件扩展名的映射
    private static final Map<String, String> MIME_TO_EXTENSION = Map.ofEntries(
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/jpg", "jpg"),
            Map.entry("image/png", "png"),
            Map.entry("image/gif", "gif"),
            Map.entry("image/webp", "webp"),
            Map.entry("image/svg+xml", "svg"),
            Map.entry("image/bmp", "bmp")
    );

    /**
     * 将 Base64 图片字符串保存为 JPG 文件
     *
     * @param base64Image 图片的 Base64 字符串 (可包含或不包含 data:image/...;base64, 头)
     * @param userId      用于命名的用户ID
     * @param targetDir   目标文件夹路径 (例如: "uploads/images")
     * @return 保存后的文件绝对路径字符串
     * @throws IOException 如果文件写入失败或目录创建失败
     */
    public static String saveBase64AsJpg(String base64Image, Long userId, String targetDir) throws IOException {

        if (base64Image == null || base64Image.trim().isEmpty()) {
            // 如果是空，直接返回，不保存图片，或者根据业务需求处理
            return "src/main/resources/avatar/empty.jpg";
        }

        // 2. 解码 Base64 为字节数组
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(removeBase64Header(base64Image).trim());
        } catch (IllegalArgumentException e) {
            throw new IOException("图片Base64 解码失败，请检查输入字符串格式", e);
        }

        // 3. 构建文件路径
        Path dirPath = Paths.get(targetDir);

        // 如果目录不存在，则创建目录 (包括父目录)
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        String userid = userId.toString();

        // 定义文件名：{userId}.jpg
        String fileName = userid + ".jpg";
        Path filePath = dirPath.resolve(fileName);

        // 4. 写入文件
        // StandardOpenOption.CREATE: 如果文件不存在则创建
        // StandardOpenOption.TRUNCATE_EXISTING: 如果文件存在，先截断（清空）再写入，实现“替换”效果
        // StandardOpenOption.WRITE: 允许写入
        Files.write(filePath, imageBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        // 获取当前工作目录 (项目根目录)
        Path currentDir = Paths.get("").toAbsolutePath();
        // 计算相对路径
        Path relativePath = currentDir.relativize(filePath.toAbsolutePath());
        return relativePath.toString();
    }
    /**
     * 保存 Base64 图片
     *
     * @param base64Image Base64 字符串 (可带 data:image/png;base64, 头)
     * @param userId 用户 ID (用于生成文件名)
     * @param targetDir 目标目录 (绝对路径或相对于工作目录的路径)
     * @return 保存后的文件相对路径 (使用正斜杠 / 分隔)
     * @throws IOException 如果 IO 操作失败或格式不正确
     */
    public static String saveBase64(String base64Image, Long userId, String targetDir) throws IOException {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new IOException("Base64 图片内容不能为空");
        }
        if (userId == null) {
            throw new IOException("用户 ID 不能为空");
        }

        // 1. 解析头部并获取纯净的 Base64 数据和 MIME Type
        Base64Data dataInfo = parseBase64Header(base64Image);
        String pureBase64 = dataInfo.content;
        String mimeType = dataInfo.mimeType;

        // 2. 解码 Base64 为字节数组
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(pureBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IOException("图片 Base64 解码失败，请检查输入字符串格式", e);
        }

        // 3. 确定文件扩展名
        String extension = getExtensionFromMime(mimeType);
        // 如果无法识别扩展名，默认使用 .bin 或抛出异常，这里选择默认 .jpg 以防万一，或者你可以选择抛错
        if (extension == null) {
            // 策略：如果无法识别，尝试从原字符串再分析，或者直接报错
            // 这里为了健壮性，如果没有匹配到，默认给一个 .img 或者根据业务需求处理
            extension = "img";
            // 或者: throw new IOException("不支持的图片格式: " + mimeType);
        }

        // 4. 构建安全的文件路径
        Path dirPath = Paths.get(targetDir).toAbsolutePath().normalize();

        // 简单的安全校验：防止 targetDir 被恶意构造跳出预期目录（如果 targetDir 是用户可控的）
        // 如果 targetDir 是后端写死的配置，这一步可以简化

        String safeUserId = userId.toString().replaceAll("[^a-zA-Z0-9_-]", "_"); // 防止特殊字符
        String fileName = safeUserId + "." + extension;
        Path filePath = dirPath.resolve(fileName).normalize();

        // 再次检查是否还在目标目录下 (防止 ../ 绕过)
        if (!filePath.startsWith(dirPath)) {
            throw new SecurityException("非法的文件路径构造");
        }

        // 如果目录不存在，则创建目录 (包括父目录)
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 5. 写入文件
        Files.write(filePath, imageBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        // 6. 计算并返回相对路径 (统一使用正斜杠 / 适配 Web 访问)
        Path currentDir = Paths.get("").toAbsolutePath();

        // 如果文件不在当前工作目录下，relativize 可能会返回绝对路径或报错，需处理
        String relativePathStr;
        if (filePath.toAbsolutePath().startsWith(currentDir)) {
            Path relativePath = currentDir.relativize(filePath.toAbsolutePath());
            relativePathStr = relativePath.toString().replace("\\", "/");
        } else {
            // 如果不在当前目录下，直接返回绝对路径的标准化形式 (替换反斜杠)
            relativePathStr = filePath.toAbsolutePath().toString().replace("\\", "/");
        }

        return relativePathStr;
    }

    /**
     * 内部类：持有解析后的数据
     */
    private static class Base64Data {
        String content;
        String mimeType;

        Base64Data(String content, String mimeType) {
            this.content = content;
            this.mimeType = mimeType;
        }
    }

    /**
     * 将 Base64 图片字符串保存为文件（用于三餐记录）
     * 文件名格式：{userId}_{mealType}_{yyyyMMddHHmmss}.jpg
     *
     * @param base64Image 图片的 Base64 字符串 (可包含或不包含 data:image/...;base64, 头)
     * @param userId      用户ID
     * @param mealType    餐食类型 (1=早餐, 2=午餐, 3=晚餐)
     * @param targetDir   目标文件夹路径 (例如: "src/main/resources/img")
     * @return 保存后的文件相对路径字符串
     * @throws IOException 如果文件写入失败或目录创建失败
     */
    public static String saveBase64ForMeal(String base64Image, Long userId, Byte mealType, String targetDir) throws IOException {
        byte[] imageBytes;
        try {
            // 移除可能存在的 "data:image/...;base64," 头部信息
            String cleanBase64 = removeBase64Header(base64Image).trim();
            imageBytes = Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            throw new IOException("图片Base64 解码失败，请检查输入字符串格式", e);
        }

        Path dirPath = Paths.get(targetDir);

        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 固定使用 .jpg 后缀，因为 mimeType 参数已移除
        String fileName = userId + "_" + mealType + "_" + timestamp + ".jpg";
        Path filePath = dirPath.resolve(fileName);

        Files.write(filePath, imageBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        Path currentDir = Paths.get("").toAbsolutePath();
        Path relativePath = currentDir.relativize(filePath.toAbsolutePath());

        return relativePath.toString();
    }
    /**
     * 去除 Base64 图片字符串的数据头，返回纯 Base64 内容。
     * 如果输入不包含数据头，则原样返回。
     * 如果输入为 null 或空，则返回 null 或空字符串。
     *
     * @param base64WithHeader 包含可能数据头的 Base64 字符串
     * @return 纯正的 Base64 字符串
     */
    public static String removeBase64Header(String base64WithHeader) {
        if (base64WithHeader == null || base64WithHeader.isEmpty()) {
            return base64WithHeader;
        }

        // 正则解释：
        // ^data:                以 "data:" 开头
        // image/\\w+;           匹配 "image/" 后跟任意单词字符(如 png, jpeg)，然后是分号
        // base64,               匹配固定的 "base64,"
        // 整个模式是大小写不敏感的
        String regex = "^data:image/\\w+;base64,";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(base64WithHeader);

        if (matcher.find()) {
            // 如果匹配到了头，则截取掉匹配到的部分
            return base64WithHeader.substring(matcher.end());
        }

        // 如果没有匹配到头（可能是纯 Base64 或者其他格式），直接返回原字符串
        // 也可以根据业务需求选择抛出异常
        return base64WithHeader;
    }

    /**
     * 解析 Base64 头，返回纯净的 Base64 字符串和 MIME Type
     */
    private static Base64Data parseBase64Header(String base64WithHeader) {
        if (base64WithHeader == null) {
            return new Base64Data("", null);
        }

        // 正则：匹配 data:image/<type>;base64,
        // 组 1: 捕获 image/<type>
        String regex = "^data:(image/\\w+[+\\w]*);base64,";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(base64WithHeader);

        String mimeType = null;
        String content = base64WithHeader;

        if (matcher.find()) {
            mimeType = matcher.group(1).toLowerCase(); // 例如: image/png
            content = base64WithHeader.substring(matcher.end());
        } else {
            // 如果没有头，尝试判断是否为纯 base64 (可选：可以根据内容特征猜测，这里暂不处理，返回 null mimeType)
            // 如果业务强制要求必须有头，可以在这里抛异常
        }

        return new Base64Data(content.trim(), mimeType);
    }

    /**
     * 根据 MIME Type 获取文件扩展名
     */
    private static String getExtensionFromMime(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        return MIME_TO_EXTENSION.get(mimeType.toLowerCase());
    }
}