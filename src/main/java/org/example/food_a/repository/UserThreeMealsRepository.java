package org.example.food_a.repository;

import org.example.food_a.entity.UserThreeMeals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户三餐记录数据交互层
 * 继承JpaRepository实现基础CRUD，扩展自定义查询方法
 */
@Repository
public interface UserThreeMealsRepository extends JpaRepository<UserThreeMeals, Long> {

    /**
     * 根据用户ID筛选近3天的三餐数据
     * @param userId 用户ID
     * @param threeDaysAgo 三天前的时间点（作为筛选起始时间）
     * @return 符合条件的三餐记录列表
     */
    List<UserThreeMeals> findByUserIdAndUpdateTimeAfter(Long userId, LocalDateTime threeDaysAgo);

    /**
     * 新增方法：按更新时间降序（越新越靠前）获取用户所有三餐数据
     * 核心：通过方法命名规则添加 ORDER BY updateTime DESC
     * @param userId 用户ID
     * @return 按时间降序排列的用户所有三餐记录
     */
    List<UserThreeMeals> findByUserIdOrderByUpdateTimeDesc(Long userId);

    /**
     * 满足“查询某用户某类餐食的所有记录并按时间排序”的场景
     * @param userId 用户ID
     * @param mealType 餐食类型（1=早餐，2=午餐，3=晚餐）
     * @return 按时间降序排列的指定类型餐食记录
     */
    List<UserThreeMeals> findByUserIdAndMealTypeOrderByUpdateTimeDesc(Long userId, Byte mealType);

}