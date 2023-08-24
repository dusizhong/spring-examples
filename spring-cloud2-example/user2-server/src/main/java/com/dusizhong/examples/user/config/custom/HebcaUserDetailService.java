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
import java.util.List;
import java.util.Set;

/**
 * 自定义河北CA登录
 * @author Dusizhong
 * @since 2022-04-20
 */
@Slf4j
@Service
public class HebcaUserDetailService {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserCaRepository sysUserCaRepository;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;
    @Autowired
    private SysGroupAreaRepo sysGroupAreaRepo;

    public UserDetails loadUserByCertAndRandomAndSign(String cert, String random, String sign) {
        if (StringUtils.isEmpty(cert)) throw new UsernameNotFoundException("证书不能为空");
        if (StringUtils.isEmpty(random)) throw new UsernameNotFoundException("随机码不能为空");
        if (StringUtils.isEmpty(sign)) throw new UsernameNotFoundException("签名不能为空");
        if(!HebcaSvsInit.verifySign(cert, random, sign)) throw new UsernameNotFoundException("CA验签失败");
        if(StringUtils.isEmpty(HebcaSvsInit.getSerialNumber(cert))) throw new UsernameNotFoundException("获取证书序列号失败");
        SysUserCa sysUserCa = sysUserCaRepository.findByCaTypeAndCaKey(CaEnum.HEBCA.getCode(), HebcaSvsInit.getUniqueId(cert));
        if(ObjectUtils.isEmpty(sysUserCa)) throw new UsernameNotFoundException("CA未绑定或已过期，请重新绑定");
        SysUser sysUser = sysUserRepository.findById(sysUserCa.getUserId()).orElse(null);
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
        myUserDetails.setCaKey(sysUserCa.getCaKey());
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
        if(!StringUtils.isEmpty(sysUser.getGroupId())) {
            List<SysGroupArea> groupAreaList = sysGroupAreaRepo.findAllByGroupId(sysUser.getGroupId());
            myUserDetails.setGroupArea(groupAreaList);
        }
        return myUserDetails;
    }
}
