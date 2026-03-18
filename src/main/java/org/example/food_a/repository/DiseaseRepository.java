package org.example.food_a.repository;

import org.example.food_a.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 疾病字典表数据访问接口
 */
@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    /**
     * 根据疾病名称查询
     * @param name 疾病名称
     * @return Optional<Disease>
     */
    Optional<Disease> findByName(String name);

    /**
     * 检查疾病名称是否存在
     * @param name 疾病名称
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * 根据名称删除疾病（慎用，如果有用户关联了该疾病可能会报错）
     */
    void deleteByName(String name);
}