package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.model.UserListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysUserRepository extends JpaRepository<SysUser, String>, JpaSpecificationExecutor<SysUser> {

    SysUser findByUsername(String username);
    SysUser findByPhone(String phone);
    SysUser findByIdCardNo(String idCardNo);

    @Query("select new com.ezjc.user.model.UserListDTO" +
            "(u.id, u.username, u.role, u.phone, u.name, u.avatar, u.groupId, u.enterpriseId, u.enabled, e.enterpriseCode, e.enterpriseName, e.status)" +
            " from SysUser u left join SysEnterprise e on u.enterpriseId = e.id where" +
            "(:username is null or u.username = :username)" +
            " and (coalesce(:roles, null) is null or u.role in :roles)" +
            " and (:phone is null or u.phone = :phone)" +
            " and (:enabled is null or u.enabled = :enabled)" +
            " and (coalesce(:enterpriseIds, null) is null or u.enterpriseId in :enterpriseIds)" +
            " and (:enterpriseName is null or e.enterpriseName like concat('%', :enterpriseName, '%'))" +
            " and (coalesce(:enterpriseStatus, null) is null or e.status in :enterpriseStatus)" +
            " order by u.createTime desc")
    Page<UserListDTO> queryList(String username, List<String> roles, String phone, Boolean enabled, List<String> enterpriseIds, String enterpriseName, List<String> enterpriseStatus, Pageable pageable);

    List<SysUser> findByEnterpriseId(String enterpriseId);

    List<SysUser> findByGroupId(String groupId);
}
