package org.example.food_a.common;

import java.util.ArrayList;
import java.util.List;

public class StringSplitter {
    public static String[] splitString(List<String> inputList) {
        // 处理列表为null 或 空列表的情况，返回空数组
        if (inputList == null || inputList.isEmpty()) {
            return new String[0];
        }

        // 用集合存储所有分割结果，方便动态添加
        List<String> resultList = new ArrayList<>();

        // 遍历列表中的每一个字符串，逐个分割
        for (String input : inputList) {
            if (input == null) {
                continue; // 跳过null元素
            }
            // 按照 |#| 分割字符串
            String[] splitParts = input.split("\\|#\\|");

            // 将分割后的所有元素添加到结果集合
            for (String part : splitParts) {
                resultList.add(part);
            }
        }

        // 集合转数组，保持返回格式不变
        return resultList.toArray(new String[0]);
    }
}