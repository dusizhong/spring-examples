package com.dusizhong.examples.log.config.custom;

import com.dusizhong.examples.log.entity.SysUser;
import com.dusizhong.examples.log.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义用户名密码登录
 * @author Dusizhong
 * @since 2023-09-20
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserRepository sysUserRepository;

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
        myUserDetails.setCredentialsNonExpired(sysUser.getCredentialsNonExpired());
        myUserDetails.setAccountNonExpired(sysUser.getAccountNonExpired());
        myUserDetails.setAccountNonLocked(sysUser.getAccountNonLocked());
        myUserDetails.setEnabled(sysUser.getEnabled());
        return myUserDetails;
    }
}
