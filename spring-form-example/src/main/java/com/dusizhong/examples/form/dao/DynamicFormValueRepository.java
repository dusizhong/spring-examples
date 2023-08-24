package com.dusizhong.examples.form.dao;

import com.dusizhong.examples.form.entity.DynamicFormValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynamicFormValueRepository extends JpaRepository<DynamicFormValue, Integer> {

    List<DynamicFormValue> findByUserIdAndFormId(Integer userId, Integer formId);
}
