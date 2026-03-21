package org.example.food_a.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.food_a.entity.AiMessage;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class AiChat {
    private static final String API_TOKEN = "sk-smbknqcgluhiwsehcgwfovmfbnqvtfzsmmxpjxieglcjidvs";
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String DEFAULT_MODEL = "Qwen/Qwen3-14B";
    private static final String VISION_MODEL = "Qwen/Qwen3-VL-8B-Instruct";

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private String callAIWithContext(String userInput, String base64Image,
                                     List<AiMessage> historyMessages,
                                     String systemPrompt) throws Exception {
        boolean hasImage = base64Image != null && !base64Image.isEmpty();
        // 【调试】打印关键入参摘要（避免打印过长的 Base64）
        System.out.println("=== AI 调用开始 ===");
        System.out.println("模式: " + (hasImage ? "多模态" : "纯文本"));
        System.out.println("历史消息数: " + historyMessages.size());
        if (hasImage) {
            System.out.println("图片前缀检测: " + (base64Image.startsWith("iVBOR") ? "PNG" :
                    base64Image.startsWith("R0lG") ? "GIF" : "JPEG/Other"));
        }
        System.out.println("UserInput: " + userInput); // 可选：打印用户输入

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", hasImage ? VISION_MODEL : DEFAULT_MODEL);
        payload.put("stream", false);
        payload.put("max_tokens", 10000);
        payload.put("temperature", 0.7);

        ArrayNode messages = objectMapper.createArrayNode();
try{
        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 复制并反转历史记录，确保顺序正确（旧 -> 新）
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

            ObjectNode imageUrl = objectMapper.createObjectNode();

            String pureBase64 = base64Image;
            String detectedPrefix = "";
            if (base64Image.contains(",")) {
                int commaIndex = base64Image.indexOf(",");
                detectedPrefix = base64Image.substring(0, commaIndex + 1);
                pureBase64 = base64Image.substring(commaIndex + 1);
            }

            String prefix;
            if (!detectedPrefix.isEmpty()) {
                prefix = detectedPrefix;
            } else {
                prefix = "data:image/jpeg;base64,";
                if (pureBase64.startsWith("iVBORw0KGgo")) {
                    prefix = "data:image/png;base64,";
                } else if (pureBase64.startsWith("R0lGODdh") || pureBase64.startsWith("R0lGODlh")) {
                    prefix = "data:image/gif;base64,";
                }
            }
            String dataUri = prefix + pureBase64;
            System.out.println("Data URI prefix: " + prefix);
            System.out.println("Pure Base64 length: " + pureBase64.length());

            imageUrl.put("url", dataUri);
            imageContent.set("image_url", imageUrl);
            contentArray.add(imageContent);

            userMsg.set("content", contentArray);
        } else {
            userMsg.put("content", userInput);
        }
        messages.add(userMsg);

        payload.set("messages", messages);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        // 【调试】打印最终请求体大小（估算）
        String requestBody = objectMapper.writeValueAsString(payload);
        System.out.println("请求体大小: " + (requestBody.length() / 1024) + " KB");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(hasImage ? 120 : 60))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 【关键检查点 1】非 200 状态码
        if (response.statusCode() != 200) {
            System.err.println("=== AI 调用失败 (HTTP " + response.statusCode() + ") ===");
            System.err.println("错误响应体: " + response.body());
            throw new Exception("API调用失败，状态码：" + response.statusCode() +
                    "，错误信息：" + response.body());
        }

        System.out.println("=== AI 调用成功，开始解析 ===");
        return parseAIResponse(response.body());

    } catch (Exception e) {
        // 【关键检查点 2】捕获所有未预期异常
        System.err.println("=== AI 调用发生异常 ===");
        System.err.println("异常类型: " + e.getClass().getName());
        System.err.println("异常信息: " + e.getMessage());
        e.printStackTrace(); // 打印完整堆栈
        throw e; // 继续向上抛出
    }
    }

    // parseAIResponse 方法保持不变
    private String parseAIResponse(String responseBody) throws Exception {
        JsonNode responseJson = objectMapper.readTree(responseBody);

        if (responseJson.has("error")) {
            JsonNode errorNode = responseJson.get("error");
            String errorMsg = errorNode.has("message") ? errorNode.get("message").asText() : "未知错误";
            throw new Exception("API返回错误：" + errorMsg);
        }

        if (!responseJson.has("choices")) {
            throw new Exception("API返回格式异常，无choices字段：" + responseBody);
        }

        JsonNode choices = responseJson.get("choices");
        if (choices == null || choices.isEmpty() || !choices.isArray()) {
            throw new Exception("API返回的choices为空或不是数组：" + responseBody);
        }

        JsonNode choice = choices.get(0);
        if (choice == null) {
            throw new Exception("API返回的choice为空：" + responseBody);
        }

        JsonNode message = choice.get("message");
        if (message == null) {
            throw new Exception("API返回的message为空：" + responseBody);
        }

        JsonNode contentNode = message.get("content");
        if (contentNode == null) {
            if (choice.has("text")) {
                return choice.get("text").asText();
            } else if (choice.has("delta") && choice.get("delta").has("content")) {
                return choice.get("delta").get("content").asText();
            }
            throw new Exception("AI回复格式异常，未找到content字段：" + responseBody);
        }

        return contentNode.asText();
    }
}
