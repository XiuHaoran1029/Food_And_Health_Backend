package org.example.food_a.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * AI消息表实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ai_message")
// 开启软删除全局过滤 (Hibernate 6+)
@SQLRestriction("delete_flag = 0")
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint COMMENT '消息ID'")
    private Long id;

    @Column(name = "conversation_id", nullable = false, columnDefinition = "bigint COMMENT '所属对话ID'")
    private Long conversationId;

    @Column(name = "user_id", nullable = false, columnDefinition = "bigint COMMENT '所属用户ID（冗余）'")
    private Long userId;

    @Column(name = "content", nullable = false, columnDefinition = "text COMMENT '消息内容'")
    private String content;

    @Column(name = "sequence", nullable = false, columnDefinition = "int COMMENT '对话内消息序号'")
    private Integer sequence;

    /**
     * 功能种类枚举映射
     * 0-AI对话，1-三餐分析，2-零食分析，3-报告识别
     * 使用 ORDINAL 模式，依赖枚举定义的顺序对应数据库的 0,1,2,3
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "function_type", columnDefinition = "tinyint default 0 COMMENT '功能种类：0-AI对话，1-三餐分析，2-零食分析，3-报告识别'")
    private FunctionType functionType; // 字段名建议同步修改，若必须保留 reply_status 可改回原名

    @Column(name = "create_time", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createTime;

    @Column(name = "delete_flag", nullable = false, columnDefinition = "tinyint default 0 COMMENT '软删除：0-未删，1-已删'")
    private Integer deleteFlag;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, columnDefinition = "varchar(20) COMMENT '角色：user/assistant/system'")
    private Role role;

    @Column(name = "img",nullable = true,length = 50,columnDefinition = "varchar(50)")
    private String img;
    /**
     * 功能种类枚举
     * ⚠️ 警告：使用 EnumType.ORDINAL 时，绝对不要改变以下枚举的顺序！
     */
    @Getter
    public enum FunctionType {
        AI_CHAT(0, "AI对话"),
        MEAL_ANALYSIS(1, "三餐分析"),
        SNACK_ANALYSIS(2, "零食分析"),
        REPORT_RECOGNITION(3, "报告识别");

        private final int code;
        private final String desc;

        FunctionType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        // 可选：通过 code 获取枚举
        public static FunctionType fromCode(int code) {
            for (FunctionType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown function type code: " + code);
        }
    }

    /**
     * 角色枚举
     */
    @Getter
    public enum Role {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system");

        private final String value;

        Role(String value) {
            this.value = value;
        }

    }

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        // 默认设置为 AI 对话
        if (this.functionType == null) {
            this.functionType = FunctionType.AI_CHAT;
        }
        if (this.deleteFlag == null) {
            this.deleteFlag = 0;
        }
    }
}