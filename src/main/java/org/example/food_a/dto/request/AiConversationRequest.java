package org.example.food_a.dto.request;

import lombok.Data;

@Data
public class AiConversationRequest {
    private Long   userId;
    private String create_time;
    private String title;
}
