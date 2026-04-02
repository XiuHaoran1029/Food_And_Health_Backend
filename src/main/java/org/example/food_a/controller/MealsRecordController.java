package org.example.food_a.controller;


import lombok.extern.slf4j.Slf4j;
import org.example.food_a.common.Result;
import org.example.food_a.service.MealsRecordService;
import org.example.food_a.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
@Slf4j
@RestController
@RequestMapping("/api/meal")
public class MealsRecordController {
    private final MealsRecordService mealsRecordService;

    public  MealsRecordController(MealsRecordService mealsRecordService) {
        this.mealsRecordService = mealsRecordService;
    }

    @GetMapping("/month")
    public Result<?> getMonthRecords(@RequestParam Integer year,
                                    @RequestParam Integer month,
                                     @RequestParam Long userId) {
        log.info("收到用户id为{}的查看月份三餐记录的请求",userId);
        HashMap<String,Object> map = new HashMap<>();
        map=mealsRecordService.getMonthRecords(year,month,userId);
        log.info("查看月份三餐记录请求已处理");
        return Result.success(map);
    }

    @GetMapping("/day")
    public Result<?> getDayDetail(@RequestParam String dateStr, @RequestParam Long userId) {
        log.info("收到用户id为{}的查看当日用户饮食记录的请求",userId);
        System.out.println(dateStr);
        HashMap<String,Object> map = new HashMap<>();
        map = mealsRecordService.getDayDetail(userId,dateStr);
        log.info("查看当日用户饮食请求已处理");
        return Result.success(map);
    }
}
