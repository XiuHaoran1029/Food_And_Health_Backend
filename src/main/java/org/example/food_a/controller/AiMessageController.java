package org.example.food_a.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.food_a.common.Result;
import org.example.food_a.dto.request.MessageRequest;
import org.example.food_a.entity.AiMessage;
import org.example.food_a.service.AiMessageService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 消息接口
 */
@Slf4j
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
        log.info("收到用户id为{}的消息",userId);
        try {
            AiMessage message = messageService.sendMessage(conversationId, userId, role, content,function_type,img,mimeType);
            log.info("成功处理用户消息");
            return Result.success(message);
        } catch (Exception e) {
            log.error("处理消息异常：{}",e.getMessage());
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
        log.info("收到用户id为{}的获取消息列表请求",userId);
        try {
            Page<AiMessage> page = messageService.getMessageList(conversationId, userId, pageNum, pageSize);
            log.info("获取消息列表请求已处理");
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取消息列表异常：{}",e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}