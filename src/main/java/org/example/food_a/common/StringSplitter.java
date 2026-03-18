package org.example.food_a.common;

public class StringSplitter {
    public static String[] splitString(String input) {
        if (input == null) {
            return new String[0];
        }
        // "|#|" 在正则中需要转义：| 变成 \\|，# 不需要转义但为了清晰也可以写上，这里主要转义 |
        // 正则表达式为：\|\#\|
        return input.split("\\|#\\|");
    }
}