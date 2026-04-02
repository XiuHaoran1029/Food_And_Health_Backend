package org.example.food_a.service;



import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.food_a.dto.response.PageChatResponse;
import org.example.food_a.entity.AiConversation;
import org.example.food_a.entity.User;
import org.example.food_a.repository.AiConversationRepository;
import org.example.food_a.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationService {
    private final AiConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * 新建对话
     */

    @Transactional
    public AiConversation createConversation(Long userId, String title) {
        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 构建对话
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setUpdateTime(LocalDateTime.now());
        log.info("用户对话构建成功");
        return conversationRepository.save(conversation);
    }

    /**
     * 分页查询用户对话
     */
    public PageChatResponse<AiConversation> getConversationList(Long userId,
                                                                Integer pageNum,
                                                                Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<AiConversation> page = conversationRepository
                .findByUserIdAndDeleteFlag(userId, 0, pageable);
        log.info("用户对话查询成功");
        // 关键：字段映射
        return new PageChatResponse<>(
                page.getContent(),
                page.getNumber() + 1,   // Spring 从 0 开始
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * 修改对话标题
     */
    @Transactional
    public AiConversation modifyConversationTitle(Long conversationId, Long userId, String title) {
        // 校验对话归属
        AiConversation conversation = conversationRepository.findByIdAndUserIdAndDeleteFlag(conversationId, userId, 0);
        if (conversation == null) {
            log.error("修改对话标题失败：对话不存在或无权限");
            throw new RuntimeException("对话不存在或无权限");
        }
        conversation.setTitle(title);
        conversation.setUpdateTime(LocalDateTime.now());
        log.info("修改对话标题成功");
        return conversationRepository.save(conversation);
    }

    /**
     * 删除对话
     */
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationRepository.findByIdAndUserIdAndDeleteFlag(conversationId, userId, 0);
        if (conversation == null) {
            throw new RuntimeException("对话不存在或无权限");
        }
        conversation.setDeleteFlag(1);
        conversation.setUpdateTime(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    /**
     * 查询单个对话
     */
    public AiConversation getConversationById(Long conversationId, Long userId) {
        return conversationRepository.findByIdAndUserIdAndDeleteFlag(conversationId, userId, 0);
    }
}