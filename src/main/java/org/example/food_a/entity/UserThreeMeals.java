package org.example.food_a.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * 用户三餐记录表实体类
 * 对应数据库表：user_three_meals
 */
@Data
@Entity
@Table(name = "user_three_meals")
@DynamicUpdate  // 仅更新有变化的字段，配合update_time自动更新
public class UserThreeMeals {

    /**
     * 主键ID，自增唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 对应MySQL的auto_increment
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * 用户ID，关联用户表的主键
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 餐食类型：1=早餐，2=午餐，3=晚餐
     * 使用Byte对应MySQL的tinyint(1)
     */
    @Column(name = "meal_type", nullable = false)
    private Byte mealType;

    /**
     * 餐食名称
     */
    @Column(name = "meal_name", nullable = false, length = 255)
    private String mealName;

    /**
     * 餐食图片URL，默认空字符串
     */
    @Column(name = "meal_pic_url", length = 512)
    private String mealPicUrl = "";

    /**
     * AI饮食建议，支持长文本
     */
    @Column(name = "ai_suggest", columnDefinition = "TEXT")
    private String aiSuggest;

    /**
     * 更新时间，自动生成/更新
     * 对应MySQL的CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    // 可选：添加餐食类型的枚举常量，方便业务使用
    public static class MealType {
        public static final Byte BREAKFAST = 1;  // 早餐
        public static final Byte LUNCH = 2;      // 午餐
        public static final Byte DINNER = 3;     // 晚餐
    }
}