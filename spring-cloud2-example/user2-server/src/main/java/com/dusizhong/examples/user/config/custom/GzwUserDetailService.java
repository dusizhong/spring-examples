package com.dusizhong.examples.user.config.custom;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.entity.SysEnterprise;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.enums.RoleEnum;
import com.dusizhong.examples.user.enums.StatusEnum;
import com.dusizhong.examples.user.util.MD5Utils;
import com.dusizhong.examples.user.util.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义国资委授权码登录
 * 用户登录国资委门户网站（必须），点击“招标系统”链接跳转到我方网站
 * 我方网站带上clientId跳转至国资委系统授权码接口地址获取code
 * 国资委同步生成code立即跳回我方指定的网站地址接收code
 * 我方网站使用code访问此接口实现一键登录
 * todo: 注意：由于服务器无法文档连接国资委vpn，所以访问国资委接口暂改为前端访问，前端并获取到国资委账号后提交后端实现一键登录（有一定安全问题）
 * @author Dusizhong
 * @since 2023-07-20
 */
@Slf4j
@Service
public class GzwUserDetailService {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;

    public UserDetails loadUserByCode(String code, String userId, String loginname, String companyNO) {
        //if (StringUtils.isEmpty(code)) throw new UsernameNotFoundException("国资委门户授权码code不能为空");
        if(StringUtils.isEmpty(userId)) throw new UsernameNotFoundException("userId不能为空");
        if(StringUtils.isEmpty(loginname)) throw new UsernameNotFoundException("loginname不能为空");
        if(StringUtils.isEmpty(companyNO)) throw new UsernameNotFoundException("companyNO不能为空");

        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("loginname", loginname);
        json.put("companyNO", companyNO);

        /**
         * 由于服务器上vpn不稳定（121好了？），暂时改为前端获取国资委用户后直接登录（有安全问题）

        // 国资委接口地址（须登录VPN访问）
        String baseUrl = "http://188.2.131.14:80";
        String clientId = "id-983cdf-45b7-da14-6275-b72a2c0a624";
        String clientSecret = "secret-228de098-f5b4-872c-c9f7-256cb7a5e829";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).build();
        try {
            HttpPost httpPost = new HttpPost(baseUrl + "/o/oauth2/token");
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("code", code));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            response = httpClient.execute(httpPost);
            JSONObject jsonObject = (JSONObject) JSONObject.parse(EntityUtils.toString(response.getEntity()));
            if(response.getStatusLine().getStatusCode() != 200) throw new UsernameNotFoundException(jsonObject.toJSONString());

            URIBuilder builder = new URIBuilder(baseUrl + "/api/jsonws/gg.oauthuser/get-oauth-user/client_id/" + clientId);
            //builder.addParameter("test", "123");
            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            httpGet.setHeader("Content-type", "application/json;charset=utf-8");
            httpGet.setHeader("Authorization", "Bearer " + jsonObject.getString("access_token"));
            response = httpClient.execute(httpGet);
            json = (JSONObject) JSONObject.parse(EntityUtils.toString(response.getEntity(), "UTF-8"));
            if(response.getStatusLine().getStatusCode() != 200) throw new UsernameNotFoundException(json.toJSONString());
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getMessage());
        } finally {
            try {
                if(httpClient != null) httpClient.close();
                if(response != null) response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
         */

        if(StringUtils.isEmpty(json.getString("loginname"))) throw new UsernameNotFoundException("获取国资委用户信息失败：loginname为空");
        if(StringUtils.isEmpty(json.getString("userId"))) throw new UsernameNotFoundException("获取国资委用户信息失败: userId为空");
        if(StringUtils.isEmpty(json.getString("companyNO"))) throw new UsernameNotFoundException("获取国资委用户信息失败: companyNO为空");

        String username = json.getString("loginname");
        String password = MD5Utils.encrypt32(json.getString("userId"));
        SysUser sysUser = sysUserRepository.findByUsername(json.getString("loginname"));
        if(ObjectUtils.isEmpty(sysUser)) {
            sysUser = new SysUser();
            sysUser.setId(SqlUtils.createId());
            sysUser.setUsername(username);
            sysUser.setPassword(new BCryptPasswordEncoder().encode(password));
            sysUser.setRole(RoleEnum.ROLE_TENDEREE.getCode() + ",ROLE_ADMIN"); //由于代理系统新增标段时未添加招标人信息，所以暂时增加ADMIN角色以在招标人系统显示全部归档标段
            sysUser.setName(json.getString("userName"));
            sysUser.setPhone(json.getString("phone"));
            sysUser.setAccountNonExpired(true);
            sysUser.setAccountNonLocked(true);
            sysUser.setCredentialsNonExpired(true);
            sysUser.setEnabled(true);
            sysUser.setStatus(StatusEnum.APPROVAL.getCode());
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findByEnterpriseCode(json.getString("companyNO"));
            if (ObjectUtils.isEmpty(sysEnterprise)) throw new UsernameNotFoundException("关联单位失败：未找到单位信息，单位信息应先同步到单位库中");
            else sysUser.setEnterpriseId(sysEnterprise.getId());
            sysUserRepository.saveAndFlush(sysUser);
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        String[] roles = sysUser.getRole().split(",");
        for (String role : roles) {
            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            authorities.add(authority);
        }
        MyUserDetails myUserDetails = new MyUserDetails();
        myUserDetails.setId(sysUser.getId());
        myUserDetails.setUsername(sysUser.getUsername());
        myUserDetails.setPassword(sysUser.getPassword());
        myUserDetails.setAuthorities(authorities);
        myUserDetails.setPhone(sysUser.getPhone());
        myUserDetails.setIdCardNo(sysUser.getIdCardNo());
        myUserDetails.setName(sysUser.getName());
        myUserDetails.setAvatar(sysUser.getAvatar());
        myUserDetails.setEnterpriseId(sysUser.getEnterpriseId());
        myUserDetails.setCredentialsNonExpired(sysUser.getCredentialsNonExpired());
        myUserDetails.setAccountNonExpired(sysUser.getAccountNonExpired());
        myUserDetails.setAccountNonLocked(sysUser.getAccountNonLocked());
        myUserDetails.setEnabled(sysUser.getEnabled());
        if(!StringUtils.isEmpty(sysUser.getEnterpriseId())) {
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(sysUser.getEnterpriseId()).orElse(null);
            if(!ObjectUtils.isEmpty(sysEnterprise)) {
                myUserDetails.setEnterpriseName(sysEnterprise.getEnterpriseName());
                myUserDetails.setEnterpriseCode(sysEnterprise.getEnterpriseCode());
            }
        }
        return myUserDetails;
    }

}
