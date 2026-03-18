package org.example.food_a.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.food_a.common.ImageSaver;
import org.example.food_a.entity.DietRestriction;
import org.example.food_a.entity.Disease;
import org.example.food_a.entity.User;
import org.example.food_a.entity.UserThreeMeals;
import org.example.food_a.repository.UserRepository;
import org.example.food_a.repository.UserThreeMealsRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodAnalysis extends AiChat {

    private final UserThreeMealsRepository userThreeMealsRepository;
    private final UserRepository userRepository;

    private static final String SYSTEM_PROMPT = "# Role\n" +
            "你是一位经验丰富、语气温和且充满关怀的饮食健康专家。你擅长通过数据分析用户的饮食习惯，结合营养学知识，提供个性化、可执行且令人愉悦的饮食建议。你的沟通风格如春风般和煦，避免使用生硬的医学术语或命令式口吻，而是像一位贴心的朋友在给予指导。\n" +
            "\n" +
            "# Context\n" +
            "用户将提供以下两部分信息：\n" +
            "1. **过去三天的饮食记录**：包含每日三餐的食物名称及当时AI给出的简要建议（用于分析用户的长期习惯、偏好及潜在的营养缺口）。\n" +
            "2. **本餐（当前餐）详情**：包含刚吃过的食物名称列表，以及该餐食物的图片（用于视觉识别分量、烹饪方式及食材细节）。\n"+
            "3. **用户疾病和忌口情况**：包含用户患有的疾病和忌口，结合这些给出下一餐建议"+
            "\n" +
            "# Task\n" +
            "请结合上述所有信息，完成以下步骤：\n" +
            "1. **综合回顾**：快速扫描过去三天的数据，找出用户近期的营养趋势（如：是否连续几天蔬菜摄入不足？蛋白质是否过量？口味是否偏重？）。\n" +
            "2. **本餐深度分析**：\n" +
            "   - 结合文字和图片，评估本餐的营养构成（碳水、蛋白质、脂肪、膳食纤维、维生素等）。\n" +
            "   - 肯定用户做得好的地方（如：选择了优质蛋白、色彩丰富等）。\n" +
            "   - 温和地指出本餐可能存在的微小不足（如：主食略多、绿叶菜稍缺、烹饪油盐可能偏重等），但不要让用户感到焦虑。\n" +
            "3. **推导下一餐建议**：\n" +
            "   - 基于\"过去三天的趋势\" + \"本餐的实际情况\"，计算为了达到全天/近期的营养均衡，**下一餐**最需要补充什么，或者需要避免什么。\n" +
            "   - 给出具体的食物推荐（最好提供2-3个具体的搭配方案），并简述推荐理由。\n" +
            "\n" +
            "# Constraints & Tone\n" +
            "- **语气要求**：温暖、鼓励、共情。多用\"我们可以尝试\"、\"建议您\"、\"看起来很不错\"等词汇，严禁使用\"必须\"、\"禁止\"、\"错误\"等强硬词汇。\n" +
            "- **逻辑性**：建议必须基于提供的历史数据和当前图片，不能凭空捏造。如果图片显示某样食物分量很大，建议在下一餐中适当调整。\n" +
            "- **实用性**：推荐的下一餐食物应是日常生活中容易获取的，避免过于昂贵或难以制作的食材。\n" +
            "- **格式清晰**：使用清晰的标题和列表，便于阅读。\n" +
            "\n" +
            "# Output Format\n" +
            "请严格按照以下结构输出回答：\n" +
            "\n" +
            "## [本餐营养小评]\n" +
            "> （在此处结合图片和文字，温和地分析本餐的亮点与可优化之处。例如：\"看到您这餐吃了清蒸鱼和西兰花，蛋白质和蔬菜的搭配非常棒！不过从图片看，米饭的分量似乎比平时稍多一些哦。\"）\n" +
            "\n" +
            "## [近期饮食趋势洞察]\n" +
            "> （在此处简要总结过去三天数据的规律。例如：\"回顾过去三天，我们发现您的早餐都很丰盛，但晚餐的蔬菜摄入量普遍略显不足。\"）\n" +
            "\n" +
            "## [下一餐专属建议]\n" +
            "为了平衡今天的营养摄入，并改善近期的饮食趋势，建议您下一餐可以尝试：\n" +
            "\n" +
            "**方案 A：[方案名称，如：清爽高纤碗]**\n" +
            "- **推荐搭配**：[具体食物，如：杂粮饭半碗 + 凉拌木耳 + 虾仁豆腐]\n" +
            "- **推荐理由**：[解释为何这样搭配能平衡本餐和近三天的不足]\n" +
            "\n" +
            "**方案 B：[方案名称，如：暖胃轻食汤]**\n" +
            "- **推荐搭配**：[具体食物]\n" +
            "- **推荐理由**：[解释理由]\n" +
            "\n" +
            "## [专家小贴士]\n" +
            "> （一句温暖的鼓励或一个实用的饮食小窍门，例如：\"记得下一餐细嚼慢咽，给身体一点消化的时间哦。\"）\n" +
            "\n" +
            "---\n" +
            "**现在，请接收用户的数据并开始分析：**\n";

    @Transactional
    public String analyzeMeal(Long userId, String role, String mealName, String imageBase64, String mimeType) throws Exception {
        byte mealType = 0;
        if(Objects.equals(role, "breakfast")){
            mealType = 1;
        }else if(Objects.equals(role, "lunch")){
            mealType = 2;
        }else if(Objects.equals(role, "dinner")){
            mealType = 3;
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<UserThreeMeals> historyMeals = userThreeMealsRepository.findByUserIdAndUpdateTimeAfter(userId, threeDaysAgo);

        String prompt = buildPrompt(user, historyMeals, mealType, mealName);

        String aiSuggestion = getAiResponseWithCustomSystem(SYSTEM_PROMPT, prompt, imageBase64, mimeType, new ArrayList<>());

        String imageUrl = "";
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                imageUrl = ImageSaver.saveBase64ForMeal(imageBase64, userId, mealType, "src/main/resources/img", mimeType);
            } catch (IOException e) {
                throw new Exception("图片保存失败：" + e.getMessage(), e);
            }
        }

        UserThreeMeals mealRecord = new UserThreeMeals();
        mealRecord.setUserId(userId);
        mealRecord.setMealType(mealType);
        mealRecord.setMealName(mealName);
        mealRecord.setMealPicUrl(imageUrl);
        mealRecord.setAiSuggest(aiSuggestion);
        mealRecord.setUpdateTime(LocalDateTime.now());

        userThreeMealsRepository.save(mealRecord);

        return aiSuggestion;
    }

    private String buildPrompt(User user, List<UserThreeMeals> historyMeals, Byte mealType, String mealName) {
        StringBuilder prompt = new StringBuilder();

        if (!user.getDietRestrictions().isEmpty() || !user.getDiseases().isEmpty()) {
            prompt.append("【用户健康信息】\n");
            
            if (!user.getDietRestrictions().isEmpty()) {
                prompt.append("饮食限制：")
                      .append(user.getDietRestrictions().stream()
                          .map(DietRestriction::getName)
                          .collect(Collectors.joining("、")))
                      .append("\n");
            }
            
            if (!user.getDiseases().isEmpty()) {
                prompt.append("疾病史：")
                      .append(user.getDiseases().stream()
                          .map(Disease::getName)
                          .collect(Collectors.joining("、")))
                      .append("\n");
            }
            prompt.append("\n");
        }

        if (historyMeals != null && !historyMeals.isEmpty()) {
            prompt.append("【过去三天的饮食记录】\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (UserThreeMeals meal : historyMeals) {
                String mealTypeName = getMealTypeName(meal.getMealType());
                String timeStr = meal.getUpdateTime().format(formatter);
                prompt.append("- ").append(timeStr).append(" ").append(mealTypeName).append("：").append(meal.getMealName()).append("\n");
                if (meal.getAiSuggest() != null && !meal.getAiSuggest().isEmpty()) {
                    String briefSuggestion = getBriefSuggestion(meal.getAiSuggest());
                    prompt.append("  AI建议：").append(briefSuggestion).append("\n");
                }
            }
            prompt.append("\n");
        }

        prompt.append("【本餐信息】\n");
        prompt.append("餐食类型：").append(getMealTypeName(mealType)).append("\n");
        prompt.append("食物名称：").append(mealName).append("\n");
        prompt.append("图片：（请结合图片进行视觉分析）\n");

        return prompt.toString();
    }

    private String getMealTypeName(Byte mealType) {
        if (mealType == null) return "未知";
        return switch (mealType) {
            case 1 -> "早餐";
            case 2 -> "午餐";
            case 3 -> "晚餐";
            default -> "未知";
        };
    }

    private String getBriefSuggestion(String aiSuggest) {
        if (aiSuggest == null || aiSuggest.isEmpty()) {
            return "无";
        }
        String cleaned = aiSuggest.replaceAll("[\\r\\n]+", " ").trim();
        if (cleaned.length() <= 100) {
            return cleaned;
        }
        return cleaned.substring(0, 100) + "...";
    }
}
