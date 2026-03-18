package org.example.food_a.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户零食食用记录表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_snack_record", comment = "用户零食食用记录表")
public class UserSnackRecord {

    /**
     * 主键ID，自增唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "bigint COMMENT '主键ID，自增唯一标识'")
    private Long id;

    /**
     * 用户ID，关联用户表主键
     */
    @Column(name = "user_id", nullable = false, columnDefinition = "bigint COMMENT '用户ID'")
    private Long userId;

    /**
     * 角色/类型：0=饮品，1=袋装零食
     */
    @Column(name = "role", nullable = false, columnDefinition = "tinyint(1) COMMENT '饮品==0，袋装零食==1'")
    private Integer role;

    /**
     * 零食记录备注名，如：下午加餐薯片
     */
    @Column(name = "snack_record_name", nullable = false, length = 255, columnDefinition = "varchar(255) DEFAULT '' COMMENT '零食记录备注名'")
    private String snackRecordName;

    /**
     * 关联零食营养表主键ID，通用零食填此值，自定义零食留NULL
     */
    @Column(name = "snack_id", columnDefinition = "bigint COMMENT '关联零食营养表主键ID'")
    private Long snackId;

    /**
     * 食用量，单位：克(g/ml)，支持小数
     */
    @Column(name = "count", nullable = false, precision = 10, columnDefinition = "double DEFAULT 0 COMMENT '食用量'")
    private Double count;

    /**
     * 记录创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    @Column(name = "update_time", nullable = false, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间'")
    private LocalDateTime updateTime;

    // 如果需要自动处理默认值（当数据库默认值不生效时），可以在 @PrePersist 中处理
    @PrePersist
    public void prePersist() {
        if (this.snackRecordName == null) {
            this.snackRecordName = "";
        }
        if (this.count == null) {
            this.count = 0.0;
        }
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.updateTime == null) {
            this.updateTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}