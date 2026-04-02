package org.example.food_a.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.food_a.common.DecryptRSA;
import org.example.food_a.common.Result;
import org.example.food_a.dto.request.LoginRequest;
import org.example.food_a.dto.request.RegisterRequest;
import org.example.food_a.dto.request.SettingRequest;
import org.example.food_a.entity.User;
import org.example.food_a.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.food_a.common.TokenGenerator.generateToken;
import static org.example.food_a.common.TokenGenerator.parseToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j  // 自动生成 log 对象
@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService, DecryptRSA RSA) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) throws Exception {

        log.info("收到登录请求");

        String email = loginRequest.getEmail();
        String password = DecryptRSA.decryptRSA(loginRequest.getPassword());

        User loginUser =userService.login(email,password);

        String token=generateToken(loginUser.getId(),loginUser.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("token",token);

        log.info("登录请求已处理");
        return Result.success(map);
    }

    /**
     * 注册接口
     * @param registerRequest 注册请求参数（自动校验）
     * @return 包含token的响应
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) throws Exception {

        log.info("收到注册请求");
        String username = registerRequest.getUsername();
        String password = DecryptRSA.decryptRSA(registerRequest.getPassword());
        String email = registerRequest.getEmail();
        User registerUser =userService.register(username,password,email);
        String token=generateToken(registerUser.getId(),registerUser.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("token",token);

        // 2. 返回成功响应
        log.info("注册请求已处理");
        return Result.success(map);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info(@RequestParam String token) throws Exception {

        String[] tokens=parseToken(token);
        String userid_s=tokens[0];
        long userId = Long.parseLong(userid_s);
        String userName=tokens[1];
        log.info("收到用户id为{}的获取用户信息请求",userId);
        Map<String,Object> map = userService.getInfo(userId,userName);
        log.info("获取用户信息请求已处理");
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
        List<String> sick=settingRequest.getSick();
        List<String> taboo=settingRequest.getTaboo();
        log.info("收到用户id为{}的修改用户信息请求",userid);
        String img = settingRequest.getImg();
        if (img == null) {
            img = settingRequest.getAvatar();
        }


        Map<String, Object>map=userService.setting(username,userid,sick,taboo,img);
        log.info("修改用户信息请求已处理");
        return Result.success(map);
    }

    @PostMapping("/setting/change")
    public Result<?> getSetting(@Valid @RequestBody SettingRequest settingRequest){

        Long userid = settingRequest.getUserid();
        log.info("收到用户id为{}的修改用户密码请求",userid);
        try{
            String old_password = DecryptRSA.decryptRSA(settingRequest.getOld_password());
            String new_password = DecryptRSA.decryptRSA(settingRequest.getNew_password());
            if(userService.getUserSetting(userid,old_password,new_password)) {
                log.info("修改用户密码请求已处理");
                return Result.success();
            }else{
                log.error("旧密码与原密码不一致或新旧密码一样");
                return Result.error("旧密码与原密码不一致或新旧密码一样");
            }
        }catch(Exception e){
            log.error("修改用户密码失败：{}",e.getMessage());
            return Result.error("修改用户密码失败"+e.getMessage());
        }

    }



}
