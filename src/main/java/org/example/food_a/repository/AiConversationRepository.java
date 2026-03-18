package org.example.food_a.repository;


import org.example.food_a.entity.AiConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    /**
     * 分页查询用户未删除的对话
     */
    Page<AiConversation> findByUserIdAndDeleteFlag(Long userId, Integer deleteFlag, Pageable pageable);

    /**
     * 查询用户未删除的对话（按更新时间倒序）
     */
    List<AiConversation> findByUserIdAndDeleteFlagOrderByUpdateTimeDesc(Long userId, Integer deleteFlag);

    /**
     * 按ID和用户ID查询未删除的对话
     */
    AiConversation findByIdAndUserIdAndDeleteFlag(Long id, Long userId, Integer deleteFlag);
}