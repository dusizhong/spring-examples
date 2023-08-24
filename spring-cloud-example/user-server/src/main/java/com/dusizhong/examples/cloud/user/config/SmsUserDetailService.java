package com.dusizhong.examples.cloud.user.config;

import com.dusizhong.examples.cloud.user.entity.SysEnterprise;
import com.dusizhong.examples.cloud.user.entity.SysUser;
import com.dusizhong.examples.cloud.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.cloud.user.repository.SysUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class SmsUserDetailService {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;

    public UserDetails loadUserByPhone(String phone, String code) {
        if (StringUtils.isEmpty(phone)) throw new UsernameNotFoundException("手机号不能为空");
        if (StringUtils.isEmpty(code)) throw new UsernameNotFoundException("验证码不能为空");
        //todo: 验证码核验
        SysUser sysUser = sysUserRepository.findByPhone(phone);
        if (sysUser == null) throw new UsernameNotFoundException("用户不存在");
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
        myUserDetails.setAvatar(sysUser.getAvatar());
        myUserDetails.setEnterpriseId(sysUser.getEnterpriseId());
        myUserDetails.setCredentialsNonExpired(sysUser.getCredentialsNonExpired());
        myUserDetails.setAccountNonExpired(sysUser.getAccountNonExpired());
        myUserDetails.setAccountNonLocked(sysUser.getAccountNonLocked());
        myUserDetails.setEnabled(sysUser.getEnabled());
        if(!StringUtils.isEmpty(sysUser.getEnterpriseId())) {
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findOne(sysUser.getEnterpriseId());
            if(!ObjectUtils.isEmpty(sysEnterprise)) {
                myUserDetails.setEnterpriseName(sysEnterprise.getEnterpriseName());
                myUserDetails.setEnterpriseCode(sysEnterprise.getEnterpriseCode());
                myUserDetails.setEnterpriseRole(sysEnterprise.getEnterpriseRole());
            }
        }
        return myUserDetails;
    }
}
