package org.example.food_a.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.food_a.entity.AiMessage;
import org.example.food_a.entity.AiConversation;
import org.example.food_a.repository.AiMessageRepository;
import org.example.food_a.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import static org.example.food_a.common.ImageSaver.saveBase64;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiMessageService {
    private final AiMessageRepository messageRepository;
    private final AiConversationService conversationService;
    private final AiChat aiChat;
    private final FoodAnalysis foodAnalysis;
    private final SnackAnalysis snackAnalysis;
    private final ReportAnalysis reportAnalysis;

    /**
     * 发送消息（带上下文记忆）
     */
    @Transactional
    public AiMessage sendMessage(Long conversationId, Long userId, String role, String content,String function_type,String imgBase64,String mimeType) throws Exception {
        // 校验对话
        AiConversation conversation = conversationService.getConversationById(conversationId, userId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在或无权限");
        }
        
        // 获取最大序号
        Integer maxSeq = messageRepository.findMaxSequenceByConversationId(conversationId);
        Integer currentSeq = (maxSeq == null ? 0 : maxSeq) + 1;

        // 构建消息
        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversation.getId());
        userMessage.setUserId(userId);
        userMessage.setRole(AiMessage.Role.USER);
        userMessage.setContent(content);
        userMessage.setSequence(currentSeq);

// 第一步：先保存以获取 ID
        userMessage = messageRepository.save(userMessage);
        Long messageId = userMessage.getId();

// 第二步：如果有图片内容，则处理图片
        if (imgBase64 != null && !imgBase64.isEmpty()) {
            // 使用 messageId 替换 userId
            String imgUrl = saveBase64(imgBase64, messageId, "src/main/resources/img", mimeType);

            // 更新消息对象的图片 URL
            userMessage.setImgUrl(imgUrl);

            // 第三步：更新保存
            messageRepository.save(userMessage);
        }
        
        // 更新对话最后更新时间
        conversation.setUpdateTime(LocalDateTime.now());

        // 查询最近10条历史消息（用于上下文记忆）
        List<AiMessage> historyMessages = messageRepository
                .findTop10ByConversationIdAndDeleteFlagOrderBySequenceDesc(conversationId, 0);
        String aiContent="";
        if(Objects.equals(function_type, "normal")){
            if(Objects.equals(imgBase64, "")){
                // 调用AI获取回复（传递上下文）
                aiContent = aiChat.getAiResponseWithContext(content, historyMessages);
            }else {
                aiContent = aiChat.getAiResponseWithContext(content,imgBase64,mimeType, historyMessages);
            }
        }else if(Objects.equals(function_type, "food_analysis")){
            aiContent = foodAnalysis.analyzeMeal(userId,role,content,imgBase64,mimeType);
        }else if(Objects.equals(function_type, "snack_analysis")){
            aiContent = snackAnalysis.analyzeSnack(userId, content, "100", "", role);
        }else if(Objects.equals(function_type, "report_analysis")){
            aiContent = reportAnalysis.analyzeReport(imgBase64,mimeType);
        }


        // 构建并保存AI回复消息
        AiMessage aiMessage = new AiMessage();
        aiMessage.setConversationId(conversation.getId());
        aiMessage.setUserId(userId);
        aiMessage.setRole(AiMessage.Role.ASSISTANT);
        aiMessage.setContent(aiContent);
        aiMessage.setSequence(currentSeq + 1);
        
        // 更新对话最后更新时间
        conversation.setUpdateTime(LocalDateTime.now());
        
        return messageRepository.save(aiMessage);
    }

    /**
     * 分页查询对话消息
     */
    public Page<AiMessage> getMessageList(Long conversationId, Long userId, Integer pageNum, Integer pageSize) {
        // 校验对话权限
        AiConversation conversation = conversationService.getConversationById(conversationId, userId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在或无权限");
        }
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "sequence"));
        return messageRepository.findByConversationIdAndDeleteFlagOrderBySequenceAsc(conversationId, 0, pageable);
    }
}