package org.example.food_a.controller;


import jakarta.validation.Valid;
import org.example.food_a.common.DecryptRSA;
import org.example.food_a.common.Result;
import org.example.food_a.dto.request.LoginRequest;
import org.example.food_a.dto.request.RegisterRequest;
import org.example.food_a.dto.request.SettingRequest;
import org.example.food_a.entity.User;
import org.example.food_a.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.example.food_a.common.TokenGenerator.generateToken;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    private final UserService userService;
    private final DecryptRSA RSA;

    @Autowired
    public UserController(UserService userService, DecryptRSA RSA) {
        this.userService = userService;
        this.RSA = RSA;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        // 1. 这里是业务逻辑占位（实际开发中需要：
        //    - 校验用户名密码是否正确
        //    - 根据用户信息生成token（user_id+username+img(base64)）
        String username = loginRequest.getUsername();
        String password = RSA.decryptRSA(loginRequest.getPassword());

        User loginUser =userService.login(username,password);

        String token=generateToken(loginUser.getId(),loginUser.getUsername(),loginUser.getAvatarUrl());
        Map<String, Object> map = new HashMap<>();
        map.put("token",token);

        // 2. 返回成功响应
        return Result.success(map);
    }

    /**
     * 注册接口
     * @param registerRequest 注册请求参数（自动校验）
     * @return 包含token的响应
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) throws Exception {
        // 1. 这里是业务逻辑占位（实际开发中需要：
        //    - 校验key是否有效
        //    - 检查用户名/邮箱是否已存在
        //    - 插入用户数据到数据库
        //    - 生成token（user_id+username+img(base64)）
        String username = registerRequest.getUsername();
        String password = RSA.decryptRSA(registerRequest.getPassword());
        String email = registerRequest.getEmail();
        User registerUser =userService.register(username,password,email);
        String token=generateToken(registerUser.getId(),registerUser.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("token",token);

        // 2. 返回成功响应
        return Result.success(map);
    }

    /**
     * 设置接口
     * @param settingRequest 注册请求参数（自动校验）
     * @return 包含token的响应
     */
    @PostMapping("/setting")
    public Result<Map<String,Object>> setting(@Valid @RequestBody SettingRequest settingRequest){
        String username = settingRequest.getUsername();
        Long userid = settingRequest.getUserid();
        String sick=settingRequest.getSick();
        String taboo=settingRequest.getTaboo();
        String img=settingRequest.getAvatar();


        Map<String, Object>map=userService.setting(username,userid,sick,taboo,img);
        return Result.success(map);
    }

    @GetMapping("/setting")
    public Result getSetting(@Valid @RequestBody SettingRequest settingRequest){
        Long userid = settingRequest.getUserid();
        String old_password = settingRequest.getOld_password();
        String new_password = settingRequest.getNew_password();
        if(userService.getUsersetting(userid,old_password,new_password)) return Result.success();
        else return Result.error("旧密码与原密码不一致或新旧密码一样");
    }



}
