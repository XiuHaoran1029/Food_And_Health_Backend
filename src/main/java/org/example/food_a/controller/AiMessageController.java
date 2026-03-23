package org.example.food_a.controller;

import lombok.RequiredArgsConstructor;
import org.example.food_a.common.Result;
import org.example.food_a.dto.request.MessageRequest;
import org.example.food_a.entity.AiMessage;
import org.example.food_a.service.AiMessageService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 消息接口
 */
@RestController
@RequestMapping("api/ai/message")
@RequiredArgsConstructor
public class AiMessageController {
    private final AiMessageService messageService;


    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<AiMessage> sendMessage(@RequestBody MessageRequest messageRequest ) {
        Long userId = messageRequest.getUserId();
        Long conversationId = messageRequest.getConversationId();
        String content = messageRequest.getContent();
        String role= messageRequest.getRole();
        String function_type= messageRequest.getFunction_type();
        String img= messageRequest.getImg();
        String mimeType= messageRequest.getMimeType();
        System.out.print("收到消息");
        try {
            System.out.println(function_type);
            AiMessage message = messageService.sendMessage(conversationId, userId, role, content,function_type,img,mimeType);
            return Result.success(message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询消息列表
     */
    @GetMapping("/list")
    public Result<Page<AiMessage>> getMessageList(
            @RequestParam Long conversationId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<AiMessage> page = messageService.getMessageList(conversationId, userId, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}