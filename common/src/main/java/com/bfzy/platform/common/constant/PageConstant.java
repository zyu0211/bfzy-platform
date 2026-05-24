package com.bfzy.platform.common.constant;

/**
 * 分页相关常量.
 *
 * @author zhangyu
 */
public final class PageConstant {

    private PageConstant() {
    }

    /**
     * 默认起始页码（从 1 开始）
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页记录数
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页记录数
     */
    public static final int MAX_PAGE_SIZE = 1000;
}
