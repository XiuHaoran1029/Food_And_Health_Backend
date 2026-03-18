package org.example.food_a.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingRequest {
    private String username;
    private Long userid;
    private String sick;
    private String taboo;
    private String avatar;
    private String old_password;
    private String new_password;
}
