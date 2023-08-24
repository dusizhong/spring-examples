package com.dusizhong.examples.user.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

public class PageHelper {

    public static Pageable of(Integer pageNumber, Integer pageSize) {
        pageNumber = ObjectUtils.isEmpty(pageNumber)? 0 : pageNumber;
        pageSize = ObjectUtils.isEmpty(pageSize)? 20 : pageSize;
        return PageRequest.of(pageNumber, pageSize, Sort.by("createTime").descending());
    }
}