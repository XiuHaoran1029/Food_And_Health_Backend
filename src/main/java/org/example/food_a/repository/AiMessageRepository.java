package org.example.food_a.repository;

import org.example.food_a.entity.AiMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    /**
     * 根据对话ID获取所有消息（按序号排序）
     */
    List<AiMessage> findByConversationIdOrderBySequenceAsc(Long conversationId);

    /**
     * 根据功能类型统计消息数量
     */
    long countByConversationIdAndFunctionType(Long conversationId, AiMessage.FunctionType functionType);

    /**
     * 查询某个用户特定功能类型的最近一条消息
     */
    List<AiMessage> findTop5ByUserIdAndFunctionTypeOrderByCreateTimeDesc(Long userId, AiMessage.FunctionType functionType);

    /**
     * 更新功能类型
     */
    @Modifying
    @Transactional
    @Query("UPDATE AiMessage m SET m.functionType = :type WHERE m.id = :id")
    int updateFunctionType(@Param("id") Long id, @Param("type") AiMessage.FunctionType type);

    /**
     * 批量更新对话中特定消息的功能类型
     */
    @Modifying
    @Transactional
    @Query("UPDATE AiMessage m SET m.functionType = :type WHERE m.conversationId = :conversationId AND m.sequence IN :sequences")
    int updateFunctionTypeBySequences(@Param("conversationId") Long conversationId,
                                      @Param("sequences") List<Integer> sequences,
                                      @Param("type") AiMessage.FunctionType type);

    /**
     * 获取指定对话的最大序号
     * 使用 JPQL 的 MAX 聚合函数，确保只返回一个值（如果没有数据则返回 null）
     */
    @Query("SELECT MAX(m.sequence) FROM AiMessage m WHERE m.conversationId = :conversationId")
    Integer findMaxSequenceByConversationId(@Param("conversationId") Long conversationId);

    List<AiMessage> findTop10ByConversationIdAndDeleteFlagOrderBySequenceDesc(Long conversationId, Integer deleteFlag);

    Page<AiMessage> findByConversationIdAndDeleteFlagOrderBySequenceAsc(Long conversationId, Integer deleteFlag, Pageable pageable);
}