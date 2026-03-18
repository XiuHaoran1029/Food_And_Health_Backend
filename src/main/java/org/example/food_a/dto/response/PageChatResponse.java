package org.example.food_a.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageChatResponse<T> {
    private List<T> content;   // 当前页数据
    private int pageNum;       // 当前页码
    private int pageSize;      // 每页条数
    private long total;        // 总条数
    private int pages;         // 总页数
}
