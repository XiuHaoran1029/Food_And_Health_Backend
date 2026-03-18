package org.example.food_a.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImageSaver {

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
    public static String saveBase64(String base64Image, Long userId, String targetDir,String mimeType) throws IOException {
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

        // 定义文件名：{userId}.mimetype
        String fileName = userid + "."+mimeType;
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
     * 将 Base64 图片字符串保存为文件（用于三餐记录）
     * 文件名格式：{userId}_{mealType}_{yyyyMMddHHmmss}.jpg
     *
     * @param base64Image 图片的 Base64 字符串 (可包含或不包含 data:image/...;base64, 头)
     * @param userId      用户ID
     * @param mealType    餐食类型 (1=早餐, 2=午餐, 3=晚餐)
     * @param targetDir   目标文件夹路径 (例如: "src/main/resources/img")
     * @param mimeType    图片类型 (例如: "jpg", "png")
     * @return 保存后的文件相对路径字符串
     * @throws IOException 如果文件写入失败或目录创建失败
     */
    public static String saveBase64ForMeal(String base64Image, Long userId, Byte mealType, String targetDir, String mimeType) throws IOException {
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(removeBase64Header(base64Image).trim());
        } catch (IllegalArgumentException e) {
            throw new IOException("图片Base64 解码失败，请检查输入字符串格式", e);
        }

        Path dirPath = Paths.get(targetDir);

        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
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
}