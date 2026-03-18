package org.example.food_a.repository;

import org.example.food_a.entity.SnackNutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 零食营养数据访问层
 */
@Repository
public interface SnackNutritionRepository extends JpaRepository<SnackNutrition, Integer> {

    /**
     * 根据零食名称精确查找
     * 由于有唯一索引，返回 Optional
     */
    Optional<SnackNutrition> findBySnackName(String snackName);

    /**
     * 模糊查询零食名称 (用于搜索功能)
     */
    List<SnackNutrition> findBySnackNameContainingIgnoreCase(String keyword);

    /**
     * 检查名称是否存在 (用于插入前校验)
     */
    boolean existsBySnackName(String snackName);

    /**
     * 查询所有低脂零食 (示例：脂肪 < 5g/100g)
     * 注意：BigDecimal 在 JPQL 中比较可能需要转为 double 或使用特定方言，这里简化演示
     */
    @Query("SELECT s FROM SnackNutrition s WHERE s.fat < :maxFat")
    List<SnackNutrition> findLowFatSnacks(@Param("maxFat") double maxFat);

    /**
     * 查询高能量零食 (示例：能量 > 2000kJ)
     */
    @Query("SELECT s FROM SnackNutrition s WHERE s.energy > :minEnergy")
    List<SnackNutrition> findHighEnergySnacks(@Param("minEnergy") double minEnergy);
}