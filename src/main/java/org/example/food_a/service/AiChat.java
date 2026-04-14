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
    @Value("${api.key}")
    private String API_TOKEN;
    @Value("${api.key}")
    private String API_TOKEN_VL ;
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String API_URL_VL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String DEFAULT_MODEL = "doubao-seed-2-0-mini-260215";
    private static final String VISION_MODEL = "doubao-seed-2-0-mini-260215";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${test.path}")
    private String webPath;

    private static final String SYSTEM_PROMPT = "# Role\n" +
            "你叫康康，是一名智能健康助手，一位兼具医学专业知识与人文关怀的虚拟健康顾问。你的核心使命是为用户提供科学、准确、易懂的健康建议，同时给予用户情感上的支持与鼓励。\n" +
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
            "- 结构：先共情（认可用户的担忧），再分析（基于上下文和常识），后建议，最后提醒。\n" +
            "- 语言：中文，流畅自然。\n" +
            "\n" +
            "# 输出格式：使用标准 Markdown\n" +
            "- 标题全部大写加粗\n" +
            "- 重点内容加粗\n" +
            "- 列表清晰\n" +
            "\n" +
            "# Initialization\n" +
            "你现在是健康助手康康，开始服务。";

    // 普通对话
    public String getAiResponseWithContext(String userInput, List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, null, historyMessages, SYSTEM_PROMPT, false);
    }

    // 带联网搜索
    public String getAiResponseWithContextAndWebSearch(String userInput, List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, null, historyMessages, SYSTEM_PROMPT, true);
    }

    // 图片+普通
    public String getAiResponseWithContext(String userInput, String base64Image, List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, base64Image, historyMessages, SYSTEM_PROMPT, false);
    }

    // 图片+搜索
    public String getAiResponseWithContextAndWebSearch(String userInput, String base64Image, List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, base64Image, historyMessages, SYSTEM_PROMPT, true);
    }

    protected String getAiResponseWithCustomSystem(String customSystemPrompt, String userInput, String base64Image, List<AiMessage> historyMessages) throws Exception {
        return callAIWithContext(userInput, base64Image, historyMessages, customSystemPrompt, false);
    }

    private String callAIWithContext(String userInput, String imageUrl, List<AiMessage> historyMessages, String systemPrompt, boolean enableWebSearch) throws Exception {
        boolean hasImage = (imageUrl != null && !imageUrl.isEmpty());
        boolean isBase64Image = hasImage && imageUrl.startsWith("data:image");

        log.info("=== AI 调用开始 ===");
        log.info("模式: {}", hasImage ? "多模态" : "纯文本");
        log.info("联网搜索: {}", enableWebSearch ? "开启" : "关闭");
        log.info("用户输入: {}", userInput);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", hasImage ? VISION_MODEL : DEFAULT_MODEL);
        payload.put("stream", false);
        payload.put("max_tokens", 10000);
        payload.put("temperature", 0.7);

        // ====================== 【核心：火山引擎官方联网搜索，最简单有效】 ======================
        if (enableWebSearch) {
            payload.put("web_search", true); // 豆包/火山引擎 官方搜索开关
        }

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

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

        if (hasImage) {
            ArrayNode contentArray = objectMapper.createArrayNode();
            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", userInput);
            contentArray.add(textContent);

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

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hasImage ? API_URL_VL : API_URL))
                .header("Authorization", "Bearer " + (hasImage ? API_TOKEN_VL : API_TOKEN))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(hasImage ? 120 : 60))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("AI调用失败 HTTP {}: {}", response.statusCode(), response.body());
            throw new Exception("API调用失败：" + response.statusCode());
        }

        log.info("=== AI 调用成功 ===");
        return parseAIResponse(response.body());
    }

    // 解析AI返回（完美修复空内容问题）
    private String parseAIResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        if (root.has("error")) {
            String msg = root.path("error").path("message").asText("AI服务异常");
            log.error("AI错误: {}", msg);
            throw new Exception(msg);
        }

        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new Exception("AI返回格式错误");
        }

        JsonNode message = choices.get(0).path("message");
        if (message.has("content") && !message.get("content").asText().isEmpty()) {
            return message.get("content").asText();
        }

        // 兜底：防止空内容
        return "我已经为你查询了最新信息，请你稍候再问我一次哦~";
    }
}