package com.dusizhong.examples.form.dao;

import com.dusizhong.examples.form.entity.DynamicFormField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynamicFormFieldRepository extends JpaRepository<DynamicFormField, Integer> {

    List<DynamicFormField> findByFormIdOrderBySortId(Integer formId);

    DynamicFormField findByFormIdAndFieldName(Integer formId, String fieldName);
}
