package org.example.food_a.dto.request;

import lombok.Data;

/**
 * AI消息请求DTO
 */
@Data
public class MessageRequest {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 对话ID
     */
    private Long conversationId;

    /**
     * 消息内容//备注
     */
    private String content;

    /**
     * 消息角色（user/assistant）//早中晚//零食种类（饮品/袋装零食）
     */
    private String role;
    /**
     *功能种类（normal food_analysis snack_analysis medication_reminder report_analysis）
     */
    private String function_type;

    /**
     * 图片消息 //零食名称
     */
    private String img;
    /**
     * 图片种类//零食数量
     */
    private String mimeType;
}
