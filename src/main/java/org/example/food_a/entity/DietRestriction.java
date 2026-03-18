package org.example.food_a.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忌口表实体类
 * 对应数据库表：diet_restriction
 */
@Entity
@Table(name = "diet_restriction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietRestriction {

    /**
     * 忌口id
     * 对应: id bigint auto_increment primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint")
    private Long id;

    /**
     * 忌口名称
     * 对应: name varchar(30) not null unique
     */
    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;
}