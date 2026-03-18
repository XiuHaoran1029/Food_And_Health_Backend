package org.example.food_a.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 疾病字典表实体类
 * 对应数据库表：disease
 */
@Entity
@Table(name = "disease")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Disease {

    /**
     * 疾病ID
     * 对应: id bigint auto_increment primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint")
    private Long id;

    /**
     * 疾病名称
     * 对应: name varchar(50) not null unique
     * 例如：感冒、肠胃炎、高血压
     */
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;
}