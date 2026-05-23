package com.bfzy.platform.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果.
 *
 * @param <T> 列表元素类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码（从 1 开始） */
    private int page;

    /** 每页记录数 */
    private int pageSize;

    /** 总页数 */
    private int totalPages;

    /** 空分页结果 */
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
                .records(Collections.emptyList())
                .total(0L)
                .page(1)
                .pageSize(20)
                .totalPages(0)
                .build();
    }

    /**
     * 构建分页结果.
     *
     * @param records  当前页数据
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页记录数
     */
    public static <T> PageResult<T> of(List<T> records, long total, int page, int pageSize) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }
}
