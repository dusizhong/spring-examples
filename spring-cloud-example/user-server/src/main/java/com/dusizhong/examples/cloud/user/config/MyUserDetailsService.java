package com.dusizhong.examples.cloud.user.config;

import com.dusizhong.examples.cloud.user.entity.SysEnterprise;
import com.dusizhong.examples.cloud.user.entity.SysUser;
import com.dusizhong.examples.cloud.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.cloud.user.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserRepository.findByUsername(username);
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
