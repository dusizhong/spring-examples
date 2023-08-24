package com.dusizhong.examples.pay.repository;

import com.dusizhong.examples.pay.entity.PayOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayOrderRepository extends JpaRepository<PayOrder, String> {
    PayOrder findByOrderNo(String orderNo);
}
