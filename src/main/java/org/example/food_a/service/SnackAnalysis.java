package org.example.food_a.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.food_a.entity.DietRestriction;
import org.example.food_a.entity.Disease;
import org.example.food_a.entity.SnackNutrition;
import org.example.food_a.entity.User;
import org.example.food_a.entity.UserSnackRecord;
import org.example.food_a.repository.SnackNutritionRepository;
import org.example.food_a.repository.UserRepository;
import org.example.food_a.repository.UserSnackRecordRepository;
import org.example.food_a.repository.UserThreeMealsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SnackAnalysis extends AiChat{
    private final UserSnackRecordRepository userSnackRecordRepository;
    private final SnackNutritionRepository snackNutritionRepository;
    private final UserRepository userRepository;

    private static final String SYSTEM_PROMPT = "# 🌟 Role: 你的贴身营养小管家\n" +
            "\n" +
            "你好！我是你的即时营养顾问。我的任务不是冷冰冰地计算数据，而是帮你轻松看懂手中的零食，用温暖、直观的方式告诉你：“这个能不能吃？”、“怎么吃更健康？”。\n" +
            "\n" +
            "## 🎯 Task\n" +
            "接收用户输入的【零食名称】和【数量】，结合营养学常识，快速评估其健康风险，并给出一份像“交通信号灯”一样直观的反馈报告。\n" +
            "\n" +
            "## 💡 Evaluation Rules (红绿灯法则)\n" +
            "请根据零食的成分、加工程度及用户输入的**具体份量**进行综合判断：\n" +
            "\n" +
            "- 🟢 **绿灯 (放心吃)**\n" +
            "  - **特征**：天然食材为主，低糖、低脂、低钠，富含膳食纤维或优质蛋白。\n" +
            "  - **场景**：如一个苹果、一小把原味坚果、一杯无糖酸奶。\n" +
            "  - **态度**：鼓励食用，作为健康加餐。\n" +
            "\n" +
            "- 🟡 **黄灯 (适量尝)**\n" +
            "  - **特征**：含有一定量的添加糖、脂肪或钠，或者是轻度加工食品。\n" +
            "  - **场景**：如几块黑巧克力、一包非油炸薯片、含糖饮料（小瓶）。\n" +
            "  - **态度**：可以享受，但必须严格控制份量，不要贪多。\n" +
            "\n" +
            "- 🔴 **红灯 (要警惕)**\n" +
            "  - **特征**：高糖、高脂、高钠，含有反式脂肪酸，属于超加工食品。\n" +
            "  - **场景**：如奶油蛋糕、辣条、炸鸡、含糖量极高的奶茶。\n" +
            "  - **态度**：建议严格限制，偶尔解馋即可，切勿作为日常习惯。\n" +
            "\n" +
            "## 🔄 Workflow (思考步骤)\n" +
            "1. **🕵️‍♀️ 洞察需求**：分析用户输入的零食是什么，特别关注**数量**（份量往往是关键）。\n" +
            "2. **⚖️ 权衡利弊**：估算总热量，并找出最大的“健康刺客”（是糖太多？盐太重？还是油太大？）。\n" +
            "3. **🚦 亮灯定级**：根据上述规则，给出对应的红绿灯等级。\n" +
            "4. **💬 暖心建议**：用一句最接地气、最具行动指导意义的话告诉用户接下来该怎么做。\n" +
            "\n" +
            "## 📝 Output Format (回复规范)\n" +
            "**重要**：为了适配手机屏幕，请**严禁使用表格**。请严格按照以下“卡片式”格式输出，不要包含任何多余的开场白或结束语：\n" +
            "\n" +
            "### 🍿 零食健康体检单\n" +
            "\n" +
            "> **🚦 健康等级**\n" +
            "> [在此处插入 🟢 / 🟡 / 🔴 图标] [等级名称，如：放心吃]\n" +
            "\n" +
            "> **⚠️ 核心风险**\n" +
            "> [一句话点出最大问题，语气要温和但明确。例：这一小包的钠含量，可能已经占了你全天额度的一半哦！]\n" +
            "\n" +
            "> **💡 贴心建议**\n" +
            "> [一句行动指南。例：今天如果吃了它，晚餐就清淡点，多喝杯水吧。]\n" +
            "\n" +
            "> **🔥 估算热量**\n" +
            "> **[数值]** kcal (基于输入份量估算)\n" +
            "\n" +
            "---\n" +
            "*温馨提示：数据仅供参考，均衡饮食才是王道哦！*";

    public String analyzeSnack(Long userId, String snackName, String count, String remark, String role){
        Integer roleValue = convertRoleToValue(role);
        Double countValue = parseCount(count);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        System.out.print("开始分析：");
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        List<UserSnackRecord> recentSnacks = 
            userSnackRecordRepository.findByUserIdAndCreateTimeAfter(userId, oneDayAgo);

        Optional<SnackNutrition> snackNutrition = 
            snackNutritionRepository.findBySnackName(snackName);

        String prompt = buildPrompt(user, recentSnacks, snackName, countValue, 
                                    remark, snackNutrition.orElse(null));

        String aiSuggestion;
        try {
            aiSuggestion = getAiResponseWithCustomSystem(
                SYSTEM_PROMPT, prompt, null, new ArrayList<>()
            );
        } catch (Exception e) {
            aiSuggestion = "AI分析暂时不可用：" + e.getMessage();
        }

        UserSnackRecord record = UserSnackRecord.builder()
            .userId(userId)
            .role(roleValue)
            .snackRecordName(remark != null && !remark.isEmpty() ? remark : snackName)
            .snackId(snackNutrition.map(SnackNutrition::getId).map(Long::valueOf).orElse(null))
            .count(countValue)
            .build();
        userSnackRecordRepository.save(record);

        return aiSuggestion;
    }

    private Integer convertRoleToValue(String role) {
        if ("饮品".equals(role)) return 0;
        if ("袋装零食".equals(role)) return 1;
        throw new IllegalArgumentException("无效的零食类型：" + role);
    }

    private Double parseCount(String count) {
        try {
            return Double.parseDouble(count);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的数量值：" + count);
        }
    }

    private String buildPrompt(User user, List<UserSnackRecord> recentSnacks, 
                              String snackName, Double count, String remark,
                              SnackNutrition nutrition) {
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

        if (!recentSnacks.isEmpty()) {
            prompt.append("【最近24小时零食记录】\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            for (UserSnackRecord snack : recentSnacks) {
                String type = snack.getRole() == 0 ? "饮品" : "零食";
                String time = snack.getCreateTime().format(formatter);
                prompt.append(String.format("- %s %s：%s %.1f克\n", 
                    time, type, snack.getSnackRecordName(), snack.getCount()));
            }
            prompt.append("\n");
        }

        prompt.append("【当前零食】\n");
        prompt.append("名称：").append(snackName).append("\n");
        prompt.append("数量：").append(count).append("克/毫升\n");
        if (remark != null && !remark.isEmpty()) {
            prompt.append("备注：").append(remark).append("\n");
        }

        if (nutrition != null) {
            prompt.append("\n【营养数据（每100克）】\n");
            prompt.append(String.format("能量：%.2f kJ\n", nutrition.getEnergy()));
            if (nutrition.getProtein() != null)
                prompt.append(String.format("蛋白质：%.2f g\n", nutrition.getProtein()));
            if (nutrition.getFat() != null)
                prompt.append(String.format("脂肪：%.2f g\n", nutrition.getFat()));
            if (nutrition.getCarbohydrate() != null)
                prompt.append(String.format("碳水化合物：%.2f g\n", nutrition.getCarbohydrate()));
            if (nutrition.getSugar() != null)
                prompt.append(String.format("糖：%.2f g\n", nutrition.getSugar()));
            if (nutrition.getSodium() != null)
                prompt.append(String.format("钠：%.2f mg\n", nutrition.getSodium()));
        } else {
            prompt.append("\n注：该零食不在营养数据库中，请基于常识判断。\n");
        }

        return prompt.toString();
    }
}
