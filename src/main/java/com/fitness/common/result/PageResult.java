package com.fitness.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total) {
        this.records = records;
        this.total = total;
    }

    /**
     * 创建分页结果
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total) {
        return new PageResult<>(records, total);
    }
}
