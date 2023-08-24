package com.dusizhong.examples.jpa.model;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

public class PageReq {

    public static Pageable of(Integer pageNum, Integer pageSize) {
        pageNum = ObjectUtils.isEmpty(pageNum)? 0 : pageNum - 1;
        pageSize = ObjectUtils.isEmpty(pageSize)? 20 : pageSize;
        return new PageRequest(pageNum, pageSize, new Sort(Sort.Direction.DESC, "createTime"));
    }
}