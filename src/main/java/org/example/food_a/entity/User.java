package org.example.food_a.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户表实体类
 * 对应数据库表：user
 */
@Entity
@Table(name = "user", comment = "用户表")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户id
     * 对应: id bigint auto_increment primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint COMMENT '用户id'")
    private Long id;

    /**
     * 用户名
     * 对应: username varchar(50) not null
     */
    @Column(name = "username", length = 50, nullable = false, columnDefinition = "varchar(50) COMMENT '用户名'")
    private String username;

    /**
     * 密码
     * 对应: password varchar(200) not null
     */
    @Column(name = "password", length = 200, nullable = false, columnDefinition = "varchar(200) COMMENT '密码'")
    private String password;

    /**
     * 邮箱
     * 对应: email varchar(50) not null
     */
    @Column(name = "email", length = 50, nullable = false, columnDefinition = "varchar(50) COMMENT '邮箱'")
    private String email;

    /**
     * 创建时间
     * 对应: create_time datetime not null
     * 使用 Java 8 LocalDateTime 映射 MySQL datetime
     */
    @Column(name = "create_time", nullable = false, columnDefinition = "datetime COMMENT '创建时间'")
    private LocalDateTime createTime;

    /**
     * 头像图片地址
     * 对应: avatar_url varchar(512) not null
     */
    @Column(name = "avatar_url", length = 512, nullable = false, columnDefinition = "varchar(512) COMMENT '头像图片地址'")
    private String avatarUrl="empty.jpg";

    /**
     * 配置多对多关系
     * targetEntity: 关联的实体类
     * joinTable: 中间表配置
     *    name: 中间表名
     *    joinColumns: 当前实体(User) 在中间表的外键列 (user_id)
     *    inverseJoinColumns: 对方实体(DietRestriction) 在中间表的外键列 (restriction_id)
     */
    @ManyToMany(fetch = FetchType.LAZY) // 建议懒加载，避免查询用户时一次性加载所有忌口
    @JoinTable(
            name = "user_diet_restriction_rel",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "restriction_id", referencedColumnName = "id")
    )
    private List<DietRestriction> dietRestrictions = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY) // 建议懒加载，避免查询用户时一次性加载所有忌口
    @JoinTable(
            name = "user_disease_rel",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "disease_id", referencedColumnName = "id")
    )
    private List<Disease> diseases = new ArrayList<>();
}