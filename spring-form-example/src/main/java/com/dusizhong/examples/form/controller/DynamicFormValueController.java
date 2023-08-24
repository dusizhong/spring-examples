package com.dusizhong.examples.form.controller;

import com.dusizhong.examples.form.dao.DynamicFormValueRepository;
import com.dusizhong.examples.form.entity.DynamicFormField;
import com.dusizhong.examples.form.entity.DynamicFormValue;
import com.dusizhong.examples.form.model.BaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/dynamicForm/value")
public class DynamicFormValueController {

    @Autowired
    private DynamicFormValueRepository dynamicFormValueRepository;

    @RequestMapping("/save")
    public BaseResp save(@RequestBody List<DynamicFormField> post) {
        if(post.isEmpty()) return BaseResp.error("动态表单数据不能为空");
        List<DynamicFormValue> oldValues = dynamicFormValueRepository.findByUserIdAndFormId(1, post.get(0).getFormId());
        dynamicFormValueRepository.delete(oldValues);
        List<DynamicFormValue> dynamicFormValueList = new ArrayList<>();
        for(DynamicFormField dynamicFormField : post) {
            DynamicFormValue dynamicFormValue = new DynamicFormValue();
            dynamicFormValue.setUserId(1);
            dynamicFormValue.setFormId(dynamicFormField.getFormId());
            dynamicFormValue.setFieldId(dynamicFormField.getId());
            dynamicFormValue.setFieldValue(dynamicFormField.getFieldValue());
            dynamicFormValue.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dynamicFormValueList.add(dynamicFormValue);
        }
        return BaseResp.success(dynamicFormValueRepository.save(dynamicFormValueList));
    }

    @RequestMapping("/list")
    public BaseResp list(@RequestBody DynamicFormValue post) {
        Example<DynamicFormValue> example = Example.of(post);
        return BaseResp.success(dynamicFormValueRepository.findAll(example));
    }
}
