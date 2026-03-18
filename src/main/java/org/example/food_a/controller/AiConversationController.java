package org.example.food_a.controller;

import lombok.RequiredArgsConstructor;
import org.example.food_a.common.Result;
import org.example.food_a.dto.request.AiConversationRequest;
import org.example.food_a.dto.response.PageChatResponse;
import org.example.food_a.entity.AiConversation;
import org.example.food_a.service.AiConversationService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话接口
 */
@RestController
@RequestMapping("/api/ai/conversation")
@RequiredArgsConstructor
public class AiConversationController {
    private final AiConversationService conversationService;

    /**
     * 新建对话
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createConversation(@RequestBody AiConversationRequest aiConversationRequest) {
        try {
            Long userId = aiConversationRequest.getUserId();
            String title = aiConversationRequest.getTitle();
            AiConversation conversation = conversationService.createConversation(userId, title);
            Map<String, Object> data = new HashMap<>();
            data.put("id", conversation.getId());
            data.put("title", title);
            data.put("userId", userId);
            data.put("create_time", conversation.getCreateTime());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询对话列表
     */
    @GetMapping("/list")
    public Result<PageChatResponse<AiConversation>> getConversationList(
            // 必传：userId（前端已校验登录，必传）
            @RequestParam Long userId,
            // 非必传：设置默认值，避免前端漏传
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 直接调用服务层，无需封装AiConversationRequest
        PageChatResponse<AiConversation> result = conversationService.getConversationList(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteConversation(
            @RequestParam Long conversationId,
            @RequestParam Long userId) {
        try {
            conversationService.deleteConversation(conversationId, userId);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}