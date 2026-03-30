package org.example.food_a.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * 用户药品服用记录表实体类
 * 对应数据库表: user_medicine
 */
@Entity
@Table(name = "user_medicine", comment = "用户药品服用记录表")
@Data // Lombok: 生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // Lombok: 无参构造
@AllArgsConstructor // Lombok: 全参构造
public class UserMedicine {

    /**
     * 药品记录主键ID
     * 对应: medicine_id bigint auto_increment primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id", columnDefinition = "bigint comment '药品记录主键ID'")
    private Long medicineId;

    /**
     * 关联用户ID
     * 对应: user_id bigint not null (foreign key)
     * 注意：这里假设你的 User 实体类名为 User，包路径需根据实际情况调整
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_medicine_user"))
    @Comment("关联用户ID，外键关联user表")
    private User user;

    /**
     * 药品名称
     * 对应: medicine_name varchar(100) not null
     */
    @Column(name = "medicine_name", length = 100, nullable = false)
    @Comment("药品名称")
    private String medicineName;

    /**
     * 每日服用次数 (1/2/3)
     * 对应: take_times tinyint default 1 not null
     */
    @Column(name = "take_times", nullable = false, columnDefinition = "tinyint default 1")
    @Comment("每日服用次数1/2/3")
    private Integer takeTimes = 1;

    /**
     * 单次服用数量
     * 对应: single_dosage bigint default 1 not null
     * 注意：虽然数据库是 bigint，但如果是药片数量通常 Integer 足够。如果确实可能超过 21亿，请改为 Long。
     * 此处严格对应数据库类型使用 Long，也可根据业务改为 Integer。
     */
    @Column(name = "single_dosage", nullable = false, columnDefinition = "bigint default 1")
    @Comment("单次服用数量")
    private Long singleDosage = 1L;

    /**
     * 停药时间
     * 对应: stop_time datetime null
     */
    @Column(name = "stop_time")
    @Comment("停药时间（精确到时分）")
    private LocalDateTime stopTime;

    /**
     * 状态 (1-正在服用 0-已停药)
     * 对应: status tinyint default 1 not null
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint default 1")
    @Comment("1-正在服用 0-已停药")
    private Integer status = 1;

    /**
     * 记录创建时间
     * 对应: create_time datetime default CURRENT_TIMESTAMP
     */
    @Column(name = "create_time", updatable = false)
    @CreationTimestamp
    @Comment("记录创建时间")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     * 对应: update_time datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP
     */
    @Column(name = "update_time")
    @UpdateTimestamp
    @Comment("记录更新时间")
    private LocalDateTime updateTime;
}