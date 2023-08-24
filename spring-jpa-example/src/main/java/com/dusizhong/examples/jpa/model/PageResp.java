package com.dusizhong.examples.jpa.model;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 自定义分页
 * 与mybaties一致
 * @author dusizhong
 * @since 2023-08-17
 */
@Data
public class PageResp<T> {

    private int pageNum;
    private int pageSize;
    private int pages;
    private long total;
    private List<T> list;

    public static <T> PageResp<T> of(Page<T> page) {
        PageResp<T> basePage = new PageResp<>();
        basePage.setPageNum(page.getNumber() + 1);
        basePage.setPageSize(page.getSize());
        basePage.setPages(page.getTotalPages());
        basePage.setTotal(page.getTotalElements());
        basePage.setList(page.getContent());
        return basePage;
    }

    public static <T> PageResp<T> of(Page<T> page, List listData) {
        PageResp<T> basePage = new PageResp<>();
        basePage.setPageNum(page.getNumber() + 1);
        basePage.setPageSize(page.getSize());
        basePage.setPages(page.getTotalPages());
        basePage.setTotal(page.getTotalElements());
        basePage.setList(listData);
        return basePage;
    }
}
