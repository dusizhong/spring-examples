package com.dusizhong.examples.form.controller;

import com.dusizhong.examples.form.dao.DynamicFormFieldRepository;
import com.dusizhong.examples.form.entity.DynamicFormField;
import com.dusizhong.examples.form.model.BaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/dynamicForm/field")
public class DynamicFormFieldController {

    @Autowired
    private DynamicFormFieldRepository dynamicFormFieldRepository;

    @PostMapping("/save")
    public BaseResp save(@RequestBody DynamicFormField post) {
        if(StringUtils.isEmpty(post.getFormId())) return BaseResp.error("表单id不能为空");
        if(StringUtils.isEmpty(post.getFieldName())) return BaseResp.error("表单项不能为空");
        if(StringUtils.isEmpty(post.getFieldType())) return BaseResp.error("类型不能为空");
        DynamicFormField exist = dynamicFormFieldRepository.findByFormIdAndFieldName(post.getFormId(), post.getFieldName());
        if(ObjectUtils.isEmpty(post.getId())) {
            if(!ObjectUtils.isEmpty(exist)) return BaseResp.error("表单项已存在");
            post.setSortId(1);
            post.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            DynamicFormField dynamicFormField = dynamicFormFieldRepository.findOne(post.getId());
            if(ObjectUtils.isEmpty(dynamicFormField)) return BaseResp.error("id无效");
            if(!ObjectUtils.isEmpty(exist) && !exist.getId().equals(dynamicFormField.getId())) return BaseResp.error("表单项已存在");
            post.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return BaseResp.success(dynamicFormFieldRepository.save(post));
    }

    @RequestMapping("list")
    public BaseResp list(@RequestBody DynamicFormField post) {
        if(ObjectUtils.isEmpty(post.getFormId())) return BaseResp.error("表单id不能为空");
        return BaseResp.success(dynamicFormFieldRepository.findByFormIdOrderBySortId(post.getFormId()));
    }

    @RequestMapping("/detail")
    public BaseResp detail(@RequestBody DynamicFormField post) {
        if(ObjectUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        DynamicFormField dynamicFormField = dynamicFormFieldRepository.findOne(post.getId());
        if(ObjectUtils.isEmpty(dynamicFormField)) return BaseResp.error("id无效");
        return BaseResp.success(dynamicFormField);
    }

    @PostMapping("/delete")
    public BaseResp delete(@RequestBody DynamicFormField post) {
        if(ObjectUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        DynamicFormField dynamicFormField = dynamicFormFieldRepository.findOne(post.getId());
        if(ObjectUtils.isEmpty(dynamicFormField)) return BaseResp.error("id无效");
        dynamicFormFieldRepository.delete(dynamicFormField);
        return BaseResp.success();
    }
}
