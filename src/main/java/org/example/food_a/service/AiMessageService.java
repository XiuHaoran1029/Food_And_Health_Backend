package org.example.food_a.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.food_a.entity.AiMessage;
import org.example.food_a.entity.AiConversation;
import org.example.food_a.repository.AiMessageRepository;
import org.example.food_a.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static  org.example.food_a.common.ImageSaver.saveAvatar;
import static org.example.food_a.common.ImageSaver.fileToBase64;


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

    @Value("${image.path1}")
    private String avatarPath;

    @Value("${image.path2}")
    private String imgPath;

    @Value("${test.path}")
    private String webPath;


    /**
     * 发送消息（带上下文记忆）
     */
    @Transactional
    public AiMessage sendMessage(Long conversationId, Long userId, String role, String content,String function_type,String imgBase64,String mimeType) throws Exception {
        // 校验对话
        AiConversation conversation = conversationService.getConversationById(conversationId, userId);
        if (conversation == null) {
            System.out.print("对话id为空");
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
        if (imgBase64 != null && imgBase64.startsWith("data:image")) {

            // 使用 messageId 替换 userId (原有逻辑)
            String imgUrl = saveAvatar(imgBase64, messageId, imgPath);

            // 更新消息对象的图片 URL
            userMessage.setImg(imgUrl);

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
                aiContent = aiChat.getAiResponseWithContext(content,userMessage.getImg(), historyMessages);
            }
        }else if(Objects.equals(function_type, "food_analysis")){
            aiContent = foodAnalysis.analyzeMeal(userId,role,content,userMessage.getImg());
        }else if(Objects.equals(function_type, "snack_analysis")){
            aiContent = snackAnalysis.analyzeSnack(userId, content, mimeType, imgBase64, role);
        }else if(Objects.equals(function_type, "report_analysis")){
            aiContent = reportAnalysis.analyzeReport(userMessage.getImg());
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
        System.out.print(aiContent);
        
        return messageRepository.save(aiMessage);
    }

    /**
     * 分页查询对话消息
     */

    public Page<AiMessage> getMessageList(Long conversationId, Long userId, Integer pageNum, Integer pageSize) {
        // 1. 校验对话权限
        AiConversation conversation = conversationService.getConversationById(conversationId, userId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在或无权限");
        }

        // 2. 分页查询
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "sequence"));
        Page<AiMessage> page = messageRepository.findByConversationIdAndDeleteFlagOrderBySequenceAsc(conversationId, 0, pageable);

        // 3. 【核心修改】遍历当前页数据，将 imgUrl 转换为 Base64
        List<AiMessage> content = page.getContent();
        if (!content.isEmpty()) {
            for (AiMessage message : content) {
                // 只有当消息包含图片且角色可能需要展示图片时转换 (根据业务需求调整)
                // 结构体4显示有 img 字段
                if (message.getImg() != null && !message.getImg().isEmpty()) {
                    // 拼接完整访问 URL
                    String fullUrl =webPath+ "/image/" + message.getImg();
                    message.setImg(fullUrl);
                }
            }
        }

        // 注意：如果直接修改了 page.getContent() 里的对象，Spring Data JPA 的 Page 实现通常会反映这些更改。
        // 但如果需要更严谨的做法，建议构造一个新的 Page 对象返回，或者使用 DTO。
        // 此处假设直接修改对象是可行的。

        return page;
    }
}