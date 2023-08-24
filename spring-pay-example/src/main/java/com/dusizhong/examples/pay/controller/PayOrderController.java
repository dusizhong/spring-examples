package com.dusizhong.examples.pay.controller;

import com.dusizhong.examples.pay.entity.PayOrder;
import com.dusizhong.examples.pay.model.BaseResp;
import com.dusizhong.examples.pay.repository.PayOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class PayOrderController {

    @Autowired
    private PayOrderRepository payOrderRepository;

    @PostMapping("/list")
    public BaseResp list(@RequestBody PayOrder post) {
        Example<PayOrder> example = Example.of(post);
        Page<PayOrder> pageData = payOrderRepository.findAll(example, new PageRequest(post.getPageNumber(), post.getPageSize(), new Sort(Sort.Direction.DESC, "createTime")));
        return BaseResp.success(pageData);
    }
}
