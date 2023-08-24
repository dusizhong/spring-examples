package com.dusizhong.examples.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.entity.SysEnterprise;
import com.dusizhong.examples.user.enums.RoleEnum;
import com.dusizhong.examples.user.enums.StatusEnum;
import com.dusizhong.examples.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.util.GzwRSAUtils;
import com.dusizhong.examples.user.util.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 国资委对接服务
 * @author dusizhong
 * @since 2023-07-18
 */
@Slf4j
@RestController
@RequestMapping("/gzw")
public class GzwController {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;

    /**
     * 组织同步接口
     * 国资委有新增或变更单位信息时调用此接口同步到我方单位库
     * 国资委用户一键登录时自动创建用户，通过用户信息中的单位代码匹配关联单位（注：可能出现单位信息未同步匹配不到，须手动处理）
     * @param sync
     * @return
     */
    @RequestMapping("/syncData")
    public String syncData(@RequestParam String sync) {
        String result = "";
        if ("1".equals(sync)) {
            Map<String, String> keyMap = GzwRSAUtils.createKeys(1024);
            String publicKey = keyMap.get("publicKey");
            String privateKey = keyMap.get("privateKey");
            String url = "http://188.2.131.14/web/system/sysDept/getEncryptionInterFaceDeptList";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            httpHeaders.set("publicKey", publicKey);
            HttpEntity<String> httpEntity = new HttpEntity<String>(null, httpHeaders);
            RestTemplate restTemplate = new RestTemplate();
            JSONObject jsonObject = restTemplate.postForObject(url, httpEntity, JSONObject.class);
            if (ObjectUtils.isEmpty(jsonObject)) return "获取国资委组织数据失败";
            if (StringUtils.isEmpty(jsonObject.getString("data"))) return "获取国资委组织数据失败：data为空";
            try {
                String decodedData = GzwRSAUtils.privateDecrypt(jsonObject.getString("data"), GzwRSAUtils.getPrivateKey(privateKey));
                JSONArray jsonArray = JSON.parseArray(decodedData);
                List<SysEnterprise> sysEnterpriseList = sysEnterpriseRepository.findAll();
                List<SysEnterprise> newList = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (!StringUtils.isEmpty(json.getString("companyNO"))) {
                        boolean isExist = false;
                        for(SysEnterprise enterprise : sysEnterpriseList) {
                            if (json.getString("companyNO").equals(enterprise.getEnterpriseCode())) {
                                isExist = true;
                                break;
                            }
                        }
                        if(!isExist) {
                            SysEnterprise sysEnterprise = new SysEnterprise();
                            sysEnterprise.setId(SqlUtils.createId());
                            sysEnterprise.setSid(System.currentTimeMillis() + RandomStringUtils.random(7, false, true));
                            sysEnterprise.setEnterpriseName(json.getString("name"));
                            sysEnterprise.setEnterpriseCode(json.getString("companyNO"));
                            sysEnterprise.setEnterpriseRole(RoleEnum.ROLE_TENDEREE.getCode());
                            sysEnterprise.setStatus(StatusEnum.APPROVAL.getCode());
                            sysEnterprise.setRegisterUser("GZW");
                            sysEnterprise.setCreateTime(SqlUtils.getDateTime());
                            newList.add(sysEnterprise);
                        }
                    }
                }
                sysEnterpriseRepository.saveAll(newList);
                result = "同步成功";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 组织基本信息同步接口
     * 暂未使用（详见ebidMin-tenderee-server中测试案例）
     * @param sync
     * @return
     */
//    @RequestMapping("/syncDetail")
//    public Resp syncDetail(@RequestParam String sync) {
//
//    }
}
