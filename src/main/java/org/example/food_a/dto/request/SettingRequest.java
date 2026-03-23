package org.example.food_a.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SettingRequest {
    private String username;
    private Long userid;

    // 从 String 改为 List<String> 列表
    private List<String> sick;

    // 从 String 改为 List<String> 列表
    private List<String> taboo;
    private String avatar;
    private String old_password;
    private String new_password;
}
