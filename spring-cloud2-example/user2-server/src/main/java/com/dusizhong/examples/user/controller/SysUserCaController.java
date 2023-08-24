package com.dusizhong.examples.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.config.custom.HebcaSvsInit;
import com.dusizhong.examples.user.config.custom.NixcaSvsInit;
import com.dusizhong.examples.user.entity.SysUserCa;
import com.dusizhong.examples.user.enums.CaEnum;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysUserCaRepository;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户CA接口
 * @author Dusizhong
 * @since 2022-09-22
 */
@RestController
@RequestMapping("/ca")
public class SysUserCaController {

    @Autowired
    private SysUserCaRepository sysUserCaRepository;

    /**
     * 绑定CA
     * @param post
     * @return
     */
    @RequestMapping(value = "/bind")
    public Resp bind(@RequestBody SysUserCa post) {
        if (StringUtils.isEmpty(post.getCaType())) return Resp.error("证书类型不能为空");
        if (StringUtils.isEmpty(post.getCert())) return Resp.error("证书不能为空");
        if (StringUtils.isEmpty(post.getRandom())) return Resp.error("随机数不能为空");
        if (StringUtils.isEmpty(post.getSign())) return Resp.error("签名不能为空");
        String userId = Oauth2Utils.getCurrentUser().getString("id");
        SysUserCa sysUserCa = sysUserCaRepository.findByUserIdAndCaType(userId, post.getCaType());
        if(ObjectUtils.isEmpty(sysUserCa)) {
            sysUserCa = new SysUserCa();
            sysUserCa.setId(SqlUtils.createId());
            sysUserCa.setUserId(userId);
        }
        if(CaEnum.HEBCA.getCode().equals(post.getCaType())) {
            if(!HebcaSvsInit.verifySign(post.getCert(), post.getRandom(), post.getSign())) return Resp.error("CA验签失败");
            String caKey = HebcaSvsInit.getUniqueId(post.getCert());
            SysUserCa exist = sysUserCaRepository.findByCaTypeAndCaKey(post.getCaType(), caKey);
            if(!ObjectUtils.isEmpty(exist)) return Resp.error("此UKEY已绑定过，不能重复绑定");
            sysUserCa.setCaType(post.getCaType());
            sysUserCa.setCaKey(caKey);
            sysUserCa.setSerialNumber(HebcaSvsInit.getSerialNumber(post.getCert()));
            sysUserCa.setSubject(HebcaSvsInit.getSubject(post.getCert()));
            sysUserCa.setIssuer(HebcaSvsInit.getIssuer(post.getCert()));
        } else if(CaEnum.ANHCA.getCode().equals(post.getCaType())) {
            Map<String, String> map = new HashMap<>();
            Map<String, String> params = new HashMap<>();
            map.put("strData", post.getRandom());
            map.put("strCert", post.getCert());
            map.put("signData", post.getSign());
            params.put("data", map.toString());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), httpHeaders);
            RestTemplate restTemplate = new RestTemplate();
            JSONObject resp = restTemplate.postForEntity("http://60.167.89.95:8881/svsapi/api/verifyAuthP1", httpEntity, JSONObject.class).getBody();
            if (!"00".equals(resp.getString("respCode"))) return Resp.error("CA验签失败");
            //todo: 安徽CA绑定信息，是否都使用自定义的方法解析公钥证书信息？

        } else if(CaEnum.NIXCA.getCode().equals(post.getCaType())) {
            if(!NixcaSvsInit.verifySign(post.getCert(), post.getRandom(), post.getSign())) return Resp.error("CA验签失败");
            String serialNumber = NixcaSvsInit.getSerialNumber(post.getCert());
            SysUserCa existSerialNumber = sysUserCaRepository.findByCaTypeAndSerialNumber(post.getCaType(), serialNumber);
            if(!ObjectUtils.isEmpty(existSerialNumber)) return Resp.error("此UKEY已绑定过，不能重复绑定");
            sysUserCa.setCaType(post.getCaType());
            sysUserCa.setCaKey(serialNumber);
            sysUserCa.setSerialNumber(serialNumber);
            sysUserCa.setSubject(NixcaSvsInit.getSubject(post.getCert()));
            sysUserCa.setIssuer(NixcaSvsInit.getIssuer(post.getCert()));
            sysUserCa.setBeginTime(NixcaSvsInit.getBeginTime(post.getCert()));
            sysUserCa.setEndTime(NixcaSvsInit.getEndTime(post.getCert()));
            sysUserCa.setAlgorithm(NixcaSvsInit.getAlgorithm(post.getCert()));
        } else return Resp.error("证书类型无效");
        return Resp.success(sysUserCaRepository.save(sysUserCa));
    }

    /**
     * 解绑CA
     * @param post
     * @return
     */
    @RequestMapping(value = "/unbind")
    public Resp unbind(@RequestBody @Valid SysUserCa post) {
        if (StringUtils.isEmpty(post.getCaType())) return Resp.error("证书类型不能为空");
        if (StringUtils.isEmpty(post.getCert())) return Resp.error("证书不能为空");
        if (StringUtils.isEmpty(post.getRandom())) return Resp.error("随机数不能为空");
        if (StringUtils.isEmpty(post.getSign())) return Resp.error("签名不能为空");
        String userId = Oauth2Utils.getCurrentUser().getString("id");
        SysUserCa sysUserCa = sysUserCaRepository.findByUserIdAndCaType(userId, post.getCaType());
        if(ObjectUtils.isEmpty(sysUserCa)) return Resp.error("未找到绑定信息");
        sysUserCaRepository.delete(sysUserCa);
        return Resp.success();
    }

    /**
     * CA验签
     * 成功返回caKey
     * @param post
     * @return
     */
    @RequestMapping(value = "/verify")
    public Resp verify(@RequestBody SysUserCa post) {
        if (StringUtils.isEmpty(post.getCaType())) return Resp.error("证书类型不能为空");
        if (StringUtils.isEmpty(post.getCert())) return Resp.error("证书不能为空");
        if (StringUtils.isEmpty(post.getRandom())) return Resp.error("随机数不能为空");
        if (StringUtils.isEmpty(post.getSign())) return Resp.error("签名不能为空");
        String caKey = null;
        if(CaEnum.HEBCA.getCode().equals(post.getCaType())) {
            if(!HebcaSvsInit.verifySign(post.getCert(), post.getRandom(), post.getSign())) return Resp.error("CA验签失败");
            caKey = HebcaSvsInit.getUniqueId(post.getCert());
        } else if(CaEnum.ANHCA.getCode().equals(post.getCaType())) {
            //todo: 安徽CA绑定信息
        } else if(CaEnum.NIXCA.getCode().equals(post.getCaType())) {
            if(!NixcaSvsInit.verifySign(post.getCert(), post.getRandom(), post.getSign())) return Resp.error("CA验签失败");
            caKey = NixcaSvsInit.getSerialNumber(post.getCert());
        } else return Resp.error("证书类型无效");
        return Resp.success(caKey);
    }
}
