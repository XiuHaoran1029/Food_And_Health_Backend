package org.example.food_a.entity;

import jakarta.persistence.*; // 如果是 Spring Boot 2.x 或旧版 Java EE，请改为 import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI对话表实体类 (JPA/Hibernate 版本)
 * Table: ai_conversation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "ai_conversation", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status")
})
public class AiConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对话ID
     * strategy = GenerationType.IDENTITY 对应 MySQL auto_increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * 所属用户ID
     * 对应数据库外键 fk_conversation_user
     * 如果不需要直接加载 User 对象，保持为 Long 类型即可
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 如果需要直接关联 User 实体，可以使用以下写法代替上面的 Long userId:
     *
     * @ManyToOne(fetch = FetchType.LAZY)
     * @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conversation_user"))
     * private User user;
     */

    /**
     * 对话标题
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 对话摘要
     * columnDefinition = "TEXT" 显式指定列类型（可选，通常类型推断即可）
     */
    @Column(name = "context_summary", columnDefinition = "TEXT")
    private String contextSummary;

    /**
     * 状态：0-已结束，1-进行中
     * 默认值建议在数据库层处理，或在 @PrePersist 中设置
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 创建时间
     * updatable = false 确保更新操作不会修改此字段
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 软删除：0-未删，1-已删
     */
    @Column(name = "delete_flag", nullable = false)
    private Integer deleteFlag = 0;

    // ================= 生命周期回调方法 =================

    /**
     * 在实体持久化（插入）之前调用
     * 用于设置 create_time 和默认值
     */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = 1;
        }
        if (this.deleteFlag == null) {
            this.deleteFlag = 0;
        }
    }

    /**
     * 在实体更新之前调用
     * 用于自动更新 update_time
     */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}