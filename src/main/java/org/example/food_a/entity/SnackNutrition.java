package org.example.food_a.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 零食营养数据表实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "snack_nutrition",
        uniqueConstraints = @UniqueConstraint(columnNames = "snack_name", name = "uk_snack_name"),
        comment = "零食营养数据表")
@Slf4j
public class SnackNutrition {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int COMMENT '主键ID'")
    private Integer id;

    /**
     * 零食名称 (唯一)
     */
    @Column(name = "snack_name", nullable = false, length = 255, columnDefinition = "varchar(255) COMMENT '零食名称'")
    private String snackName;

    /**
     * 能量 (kJ) - 必填
     * 对应 decimal(8, 2)
     */
    @Column(name = "energy", nullable = false, precision = 8, columnDefinition = "decimal(8,2) COMMENT '能量(kJ)'")
    private BigDecimal energy;

    /**
     * 蛋白质 (g/100g)
     */
    @Column(name = "protein", precision = 6, columnDefinition = "decimal(6,2) COMMENT '蛋白质含量'")
    private BigDecimal protein;

    /**
     * 脂肪 (g/100g)
     */
    @Column(name = "fat", precision = 6,  columnDefinition = "decimal(6,2) COMMENT '脂肪含量'")
    private BigDecimal fat;

    /**
     * 饱和脂肪 (g/100g)
     */
    @Column(name = "fat_saturated", precision = 6, columnDefinition = "decimal(6,2) COMMENT '饱和脂肪'")
    private BigDecimal fatSaturated;

    /**
     * 碳水化合物 (g/100g)
     */
    @Column(name = "carbohydrate", precision = 6,  columnDefinition = "decimal(6,2) COMMENT '碳水化合物'")
    private BigDecimal carbohydrate;

    /**
     * 糖含量 (g/100g)
     */
    @Column(name = "sugar", precision = 6, columnDefinition = "decimal(6,2) COMMENT '糖含量'")
    private BigDecimal sugar;

    /**
     * 钠含量 (mg/100g)
     * 注意：数据库定义为 int，这里用 Integer
     */
    @Column(name = "sodium", columnDefinition = "int COMMENT '钠含量'")
    private Integer sodium;

    /**
     * 膳食纤维 (g/100g)
     */
    @Column(name = "dietary_fiber", precision = 6,  columnDefinition = "decimal(6,2) COMMENT '膳食纤维'")
    private BigDecimal dietaryFiber;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    // 自动填充时间戳逻辑 (如果数据库层面未生效或需要Java层控制)
    @PrePersist
    public void prePersist() {
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