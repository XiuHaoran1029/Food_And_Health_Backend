package org.example.food_a.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.example.food_a.entity.AiMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class AiChat {
    private static final String API_TOKEN = "****";
    private static final String API_TOKEN_VL="****";
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String API_URL_VL="https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String DEFAULT_MODEL = "doubao-seed-2-0-mini-260215";
    private static final String VISION_MODEL = "doubao-seed-2-0-mini-260215";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${test.path}")
    private String webPath;

    private static final String SYSTEM_PROMPT = "# Role\n" +
            "你是一名智能健康助手，一位兼具医学专业知识与人文关怀的虚拟健康顾问。你的核心使命是为用户提供科学、准确、易懂的健康建议，同时给予用户情感上的支持与鼓励。\n" +
            "\n" +
            "# Goals\n" +
            "1. 专业解答：基于权威医学指南和最新科研共识，回答用户关于疾病预防、症状分析、营养饮食、运动健身、心理健康及用药咨询等问题。\n" +
            "2. 上下文感知：紧密结合对话历史，理解用户的健康状况、既往病史及个人偏好，提供个性化的建议，避免重复询问已知信息。\n" +
            "3. 友好互动：保持温暖、耐心、非评判性的语气，让用户感到被倾听和被关心。\n" +
            "4. 风险管控：严格识别紧急医疗状况，并在必要时引导用户寻求线下专业医疗帮助。\n" +
            "\n" +
            "# Constraints & Safety Guidelines (至关重要)\n" +
            "1. 非诊断声明：你不能替代执业医师进行确诊或开具处方。在涉及具体疾病诊断时，必须明确说明：我是AI助手，以下建议仅供参考，不能替代专业医生的诊断。如有不适，请及时就医。\n" +
            "2. 紧急情况处理：如果用户描述的症状涉及生命危险（如胸痛、呼吸困难、大出血、严重过敏反应、自杀倾向等），必须第一时间强烈建议用户拨打急救电话（如中国120）或立即前往最近的急诊室，停止提供常规建议。\n" +
            "3. 证据导向：对于不确定的医学信息，不要编造。\n" +
            "4. 隐私保护：提醒用户不要在对话中透露真实的姓名、身份证号、详细住址等敏感个人隐私信息。\n" +
            "\n" +
            "# Interaction Style\n" +
            "- 语气：亲切、专业、冷静、充满同理心。避免使用过于生硬的医学术语，若必须使用，请用通俗语言解释。\n" +
            "- 结构：先共情（认可用户的担忧），再分析（基于上下文和医学常识），后建议（提供可执行的步骤，分点陈述），最后提醒（必要的免责或就医建议）。\n" +
            "- 语言：默认使用与用户相同的语言（中文为主），表达清晰流畅。\n" +
            "\n" +
            "# Workflow\n" +
            "1. 分析上下文：回顾之前的对话，提取关键健康指标和用户意图。\n" +
            "2. 评估风险：判断是否属于紧急医疗状况。如果是，执行紧急预案。\n" +
            "3. 检索/调用知识：在允许的情况下使用联网搜索工具验证最新指南获取信息。\n" +
            "4. 生成回复：按照共情 -> 分析 -> 建议 -> 提醒的结构组织语言。\n" +
            "5. 自我审查：检查是否包含绝对化的诊断词汇，若有则修正为可能性描述。\n" +
            "\n" +
            "\"- **Markdown 排版规范（重要）**：\\n\" +\n" +
            "  - **必须全程使用标准的 Markdown 格式**进行回复，以增强可读性。\\n\" +\n" +
            "  - **标题样式**：所有一级标题（##）和二级标题（###）中的核心文字必须 **全部大写** 并 **加粗**。" +
            "  - **强调重点**：关键结论,或重要建议请使用 **加粗** (`**text**`) 突出显示。" +
            "  - **引用块**：分析性、描述性的段落请使用引用块 (`>`) 包裹，营造对话感." +
            "  - **列表**：推荐方案必须使用清晰的无序列表 (`-`) 或有序列表，保持结构整洁。" +
            "  - **分隔线**：不同大板块之间使用分隔线 (`---`) 进行视觉隔离。"+
            "# Initialization\n" +
            "现在，请准备好以智能健康助手的身份开始服务。请等待用户的消息，并始终记住：安全第一，专业为本，温暖相伴。";

    public String getAiResponseWithContext(String userInput, List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, null, historyMessages, SYSTEM_PROMPT);
    }

    public String getAiResponseWithContext(String userInput, String base64Image,
                                           List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, base64Image, historyMessages, SYSTEM_PROMPT);
    }

    protected String getAiResponseWithCustomSystem(String customSystemPrompt, String userInput,
                                                   String base64Image,
                                                   List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, base64Image, historyMessages, customSystemPrompt);
    }

    private String callAIWithContext(String userInput, String imageUrl,
                                     List<AiMessage> historyMessages,
                                     String systemPrompt) throws Exception {
        // 图片可以是 data URL(base64) 或文件名
        boolean hasImage = imageUrl != null && !imageUrl.isEmpty();
        boolean isBase64Image = hasImage && imageUrl.startsWith("data:image");

        // 调试日志
        log.info("=== AI 调用开始 ===");
        log.info("模式: {}", hasImage ? "多模态" : "纯文本");
        log.info("历史消息数: {}", historyMessages.size());
        if (hasImage) {
            if (isBase64Image) {
                log.info("图片Base64长度: {}", imageUrl.length());
            } else {
                log.info("图片URL: {}", imageUrl);
            }
        }
        log.info("用户输入: {}", userInput);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", hasImage ? VISION_MODEL : DEFAULT_MODEL);
        payload.put("stream", false);
        payload.put("max_tokens", 10000);
        payload.put("temperature", 0.7);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 历史消息
        List<AiMessage> orderedHistory = new java.util.ArrayList<>(historyMessages);
        java.util.Collections.reverse(orderedHistory);
        for (AiMessage msg : orderedHistory) {
            ObjectNode historyMsg = objectMapper.createObjectNode();
            historyMsg.put("role", msg.getRole().getValue());
            historyMsg.put("content", msg.getContent());
            messages.add(historyMsg);
        }

        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");

        // 【修改2】核心：使用 URL 构造多模态消息
        if (hasImage) {
            ArrayNode contentArray = objectMapper.createArrayNode();

            // 文本
            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", userInput);
            contentArray.add(textContent);

            // 图片（支持 data URL 或 文件名）
            ObjectNode imageContent = objectMapper.createObjectNode();
            imageContent.put("type", "image_url");

            ObjectNode urlObj = objectMapper.createObjectNode();
            String finalUrl = isBase64Image ? imageUrl : webPath + "/image/" + imageUrl;
            urlObj.put("url", finalUrl);

            imageContent.set("image_url", urlObj);
            contentArray.add(imageContent);

            userMsg.set("content", contentArray);
        } else {
            userMsg.put("content", userInput);
        }
        messages.add(userMsg);
        payload.set("messages", messages);

        // 发送请求
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        String requestBody = objectMapper.writeValueAsString(payload);
        log.info("请求体大小: {} KB", requestBody.length() / 1024);

        HttpRequest request;
        if (hasImage) {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL_VL))
                    .header("Authorization", "Bearer " + API_TOKEN_VL)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(120))
                    .build();
        } else {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();
        }

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("AI调用失败 HTTP {}", response.statusCode());
            log.error("错误信息: {}", response.body());
            throw new Exception("API调用失败：" + response.statusCode());
        }

        log.info("=== AI 调用成功 ===");;
        return parseAIResponse(response.body());
    }

    // parseAIResponse 方法保持不变
    private String parseAIResponse(String responseBody) throws Exception {
        JsonNode responseJson = objectMapper.readTree(responseBody);

        if (responseJson.has("error")) {
            JsonNode errorNode = responseJson.get("error");
            String errorMsg = errorNode.has("message") ? errorNode.get("message").asText() : "未知错误";
            log.error("API返回错误：{}", errorMsg);
            throw new Exception("API返回错误：" + errorMsg);
        }

        if (!responseJson.has("choices")) {
            log.error("API返回格式异常，无choices字段：{}", responseBody);
            throw new Exception("API返回格式异常，无choices字段：" + responseBody);
        }

        JsonNode choices = responseJson.get("choices");
        if (choices == null || choices.isEmpty() || !choices.isArray()) {
            log.error("API返回的choices为空或不是数组：{}", responseBody);
            throw new Exception("API返回的choices为空或不是数组：" + responseBody);
        }

        JsonNode choice = choices.get(0);
        if (choice == null) {
            log.error("API返回的choice为空：{}", responseBody);
            throw new Exception("API返回的choice为空：" + responseBody);
        }

        JsonNode message = choice.get("message");
        if (message == null) {
            log.error("API返回的message为空：" + responseBody);
            throw new Exception("API返回的message为空：" + responseBody);
        }

        JsonNode contentNode = message.get("content");
        if (contentNode == null) {
            if (choice.has("text")) {
                return choice.get("text").asText();
            } else if (choice.has("delta") && choice.get("delta").has("content")) {
                return choice.get("delta").get("content").asText();
            }
            log.error("API返回的message为空：" + responseBody);
            throw new Exception("API返回的message为空：" + responseBody);
        }

        return contentNode.asText();
    }
}
