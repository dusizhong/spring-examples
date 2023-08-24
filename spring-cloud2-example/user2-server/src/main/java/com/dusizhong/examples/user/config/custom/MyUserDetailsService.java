package com.dusizhong.examples.user.config.custom;

import com.dusizhong.examples.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.user.repository.SysGroupAreaRepo;
import com.dusizhong.examples.user.repository.SysUserCaRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.entity.SysEnterprise;
import com.dusizhong.examples.user.entity.SysGroupArea;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.entity.SysUserCa;
import com.dusizhong.examples.user.enums.CaEnum;
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
import java.util.List;
import java.util.Set;

/**
 * 自定义用户名密码登录
 * @author Dusizhong
 * @since 2022-08-18
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserCaRepository sysUserCaRepository;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;
    @Autowired
    private SysGroupAreaRepo sysGroupAreaRepo;

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
        myUserDetails.setName(sysUser.getName());
        myUserDetails.setAvatar(sysUser.getAvatar());
        myUserDetails.setEnterpriseId(sysUser.getEnterpriseId());
        myUserDetails.setCredentialsNonExpired(sysUser.getCredentialsNonExpired());
        myUserDetails.setAccountNonExpired(sysUser.getAccountNonExpired());
        myUserDetails.setAccountNonLocked(sysUser.getAccountNonLocked());
        myUserDetails.setEnabled(sysUser.getEnabled());
        SysUserCa sysUserCa = sysUserCaRepository.findByUserIdAndCaType(sysUser.getId(), CaEnum.HEBCA.getCode());
        if(!ObjectUtils.isEmpty(sysUserCa)) {
            myUserDetails.setCaKey(sysUserCa.getCaKey());
        }
        if(!StringUtils.isEmpty(sysUser.getEnterpriseId())) {
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(sysUser.getEnterpriseId()).orElse(null);
            if(!ObjectUtils.isEmpty(sysEnterprise)) {
                myUserDetails.setEnterpriseName(sysEnterprise.getEnterpriseName());
                myUserDetails.setEnterpriseCode(sysEnterprise.getEnterpriseCode());
            }
        }
        if(!StringUtils.isEmpty(sysUser.getGroupId())) {
            List<SysGroupArea> groupAreaList = sysGroupAreaRepo.findAllByGroupId(sysUser.getGroupId());
            myUserDetails.setGroupArea(groupAreaList);
        }
        return myUserDetails;
//        return new org.springframework.security.core.userdetails.User (
//                sysUser.username,
//                sysUser.password,
//                sysUser.enabled,
//                true,
//                true,
//                true,
//                AuthorityUtils.commaSeparatedStringToAuthorityList(sysUser.role));
    }
}
