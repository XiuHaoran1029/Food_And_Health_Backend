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

    private static final String SYSTEM_PROMPT ="# Role\n" +
            "你是健康管理系统的即时营养顾问。\n" +
            "\n" +
            "# Task\n" +
            "接收用户输入的零食名称和数量，快速判断其健康风险等级，并给出“红绿灯”式的反馈。\n" +
            "\n" +
            "# Rules\n" +
            "- \uD83D\uDFE2 绿灯：低糖、低脂、低钠，天然食材为主。\n" +
            "- \uD83D\uDFE1 黄灯：适量食用，注意份量，含有一定添加糖或脂肪。\n" +
            "- \uD83D\uDD34 红灯：高糖、高脂、高钠，超加工食品，建议严格限制。\n" +
            "\n" +
            "# Workflow\n" +
            "1. 识别零食类型。\n" +
            "2. 估算总热量和核心风险因子（糖/钠/反式脂肪）。\n" +
            "3. 判定红绿灯等级。\n" +
            "4. 用一句话给出最核心的建议。\n" +
            "\n" +
            "# Output Format\n" +
            "仅返回以下文本块：\n" +
            "【等级】: [\uD83D\uDFE2/\uD83D\uDFE1/\uD83D\uDD34]\n" +
            "【风险提示】: [一句话指出最大问题，如：这一包的钠含量已超过全天建议量的50%]\n" +
            "【核心建议】: [一句话行动指南]\n" +
            "【估算热量】: [数值] kcal\n" +
            "\n" +
            "# User Input\n" +
            "{{user_input_string}}";

    @Transactional
    public String analyzeSnack(Long userId, String snackName, String count, String remark, String role){
        Integer roleValue = convertRoleToValue(role);
        Double countValue = parseCount(count);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

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
                SYSTEM_PROMPT, prompt, null, null, new ArrayList<>()
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
