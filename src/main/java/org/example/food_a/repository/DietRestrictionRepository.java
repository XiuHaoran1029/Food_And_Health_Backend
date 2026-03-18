package org.example.food_a.repository;
import org.example.food_a.entity.DietRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 忌口表数据访问接口
 */
@Repository
public interface DietRestrictionRepository extends JpaRepository<DietRestriction, Long> {

    /**
     * 根据名称查询忌口信息
     * 由于数据库有唯一约束，这里返回 Optional 或单个对象均可
     */
    Optional<DietRestriction> findByName(String name);

    /**
     * 检查名称是否已存在
     * 用于在保存前快速验证，避免抛出 DuplicateKeyException
     */
    boolean existsByName(String name);

    /**
     * 根据名称删除
     */
    void deleteByName(String name);
}