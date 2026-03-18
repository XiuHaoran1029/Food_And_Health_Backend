package org.example.food_a.repository;

import org.example.food_a.entity.UserSnackRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户零食记录数据访问层
 */
@Repository
public interface UserSnackRecordRepository extends JpaRepository<UserSnackRecord, Long> {

    /**
     * 根据用户ID查询所有记录
     */
    List<UserSnackRecord> findByUserId(Long userId);

    /**
     * 根据用户ID和类型查询 (0:饮品, 1:零食)
     */
    List<UserSnackRecord> findByUserIdAndRole(Long userId, Integer role);

    /**
     * 查询某个用户在特定时间范围内的记录
     */
    List<UserSnackRecord> findByUserIdAndCreateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 统计用户当天的总摄入量 (按克/ml)
     * 注意：如果结果为空，SUM可能返回null，调用时需处理
     */
    @Query("SELECT COALESCE(SUM(r.count), 0) FROM UserSnackRecord r WHERE r.userId = :userId AND r.createTime >= :startOfDay")
    Double sumCountByUserIdAndDate(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 删除用户的所有记录（慎用）
     */
    void deleteByUserId(Long userId);

    /**
     * 查询用户在指定时间之后的零食记录
     */
    List<UserSnackRecord> findByUserIdAndCreateTimeAfter(Long userId, LocalDateTime after);
}