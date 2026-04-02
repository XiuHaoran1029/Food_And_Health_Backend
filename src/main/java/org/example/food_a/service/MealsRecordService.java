package org.example.food_a.service;

import lombok.extern.slf4j.Slf4j;
import org.example.food_a.common.ImageSaver;
import org.example.food_a.entity.UserSnackRecord;
import org.example.food_a.entity.UserThreeMeals;
import org.example.food_a.repository.UserSnackRecordRepository;
import org.example.food_a.repository.UserThreeMealsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MealsRecordService {
    private final UserThreeMealsRepository userThreeMealsRepository;
    private final UserSnackRecordRepository userSnackRecordRepository;
    public MealsRecordService(UserThreeMealsRepository userThreeMealsRepository, UserSnackRecordRepository userSnackRecordRepository) {
        this.userThreeMealsRepository = userThreeMealsRepository;
        this.userSnackRecordRepository = userSnackRecordRepository;
    }

    public HashMap<String,Object> getMonthRecords(Integer year, Integer month, Long userId)
    {
        HashMap<String,Object> map = new HashMap<>();

        List<UserThreeMeals> userThreeMealsList =
                userThreeMealsRepository.findByUserIdAndUpdateTimeYearAndMonth(
                        userId, year, month
                );

        // 提取日期 → 转成 yyyy-MM-dd → 去重 → 排序
        List<String> recordDays = userThreeMealsList.stream()
                .map(UserThreeMeals::getUpdateTime)  // 获取更新时间
                .map(LocalDateTime::toLocalDate)     // 只保留日期（去掉时分秒）
                .map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .distinct()  // 同一天去重
                .sorted()    // 按日期排序
                .collect(Collectors.toList());
        log.info("本月共查询到{}天记录",recordDays.size());

        // 放入你要的结构
        map.put("recordDays", recordDays);
        return map;
    }

    public HashMap<String, Object> getDayDetail(Long userId, String date) {
        HashMap<String, Object> resultMap = new HashMap<>();

        // 1. 把传入的日期字符串转成 LocalDate
        LocalDate queryDate = LocalDate.parse(date);

        resultMap.put("date", queryDate);

        // 2. 查询【该用户 + 当天】的所有三餐记录（3条以内）
        List<UserThreeMeals> mealsList = userThreeMealsRepository
                .findByUserIdAndUpdateTimeBetween(
                        userId,
                        queryDate.atStartOfDay(),                  // 当天 00:00:00
                        queryDate.plusDays(1).atStartOfDay()        // 次日 00:00:00
                );
        log.info("当日共{}条三餐记录",mealsList.size());
        // 3. 封装成你要的格式：breakfast / lunch / dinner
        resultMap.put("breakfast", buildMealMap(mealsList, 1));
        resultMap.put("lunch", buildMealMap(mealsList, 2));
        resultMap.put("dinner", buildMealMap(mealsList, 3));

        List<UserSnackRecord> userSnackRecords = userSnackRecordRepository.findByUserIdAndCreateTimeBetween(
                userId,
                queryDate.atStartOfDay(),                  // 当天 00:00:00
                queryDate.plusDays(1).atStartOfDay()
        );

        List<Map<String, Object>> snackList = userSnackRecords.stream().map(record -> {
            Map<String, Object> snackMap = new HashMap<>();
            snackMap.put("snack_name", record.getSnackName());
            snackMap.put("remark", record.getRemark());
            snackMap.put("role", record.getRole());
            snackMap.put("count", record.getCount());
            return snackMap;
        }).collect(Collectors.toList());
        log.info("当日共{}条零食记录",snackList.size());

        resultMap.put("snack", snackList);

        return resultMap;
    }

    /**
     * 根据 meal_type 从列表中找到对应餐食，并封装成 Map
     * @param list 当天所有餐食
     * @param mealType 1=早餐 2=午餐 3=晚餐
     * @return 封装好的餐食对象 map
     */
    private Map<String, Object> buildMealMap(List<UserThreeMeals> list, int mealType) {
        Map<String, Object> mealMap = new HashMap<>();

        // 找到对应类型的餐食
        UserThreeMeals meal = list.stream()
                .filter(m -> m.getMealType() == mealType)
                .findFirst()
                .orElse(null);

        if (meal != null) {
            mealMap.put("imageUrl", meal.getMealPicUrl());
            mealMap.put("comment", meal.getMealName());
            mealMap.put("suggest", meal.getAiSuggest() == null ? "" : meal.getAiSuggest());
        } else {
            // 没记录时返回空结构
            mealMap.put("imageUrl", "");
            mealMap.put("comment", "");
            mealMap.put("suggest", "");
        }

        return mealMap;
    }
}
