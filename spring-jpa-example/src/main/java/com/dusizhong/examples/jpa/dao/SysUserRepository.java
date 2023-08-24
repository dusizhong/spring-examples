package com.dusizhong.examples.jpa.dao;

import com.dusizhong.examples.jpa.entity.SysUser;
import com.dusizhong.examples.jpa.model.UserListVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SysUserRepository extends JpaRepository<SysUser, String>, JpaSpecificationExecutor<SysUser> {

    SysUser findByUsername(String username);
    SysUser findByPhone(String phone);
    SysUser findByIdCardNo(String phone);

    //todo: 联表写法有问题，不支持left join on，默认成cross join了?，批量查询无效。（在spring boot2.x中联表查询写法不同，详见我的ezjc-user项目）
    @Query("select new com.dusizhong.examples.jpa.model.UserListVO" +
            "(u.id, u.username, u.role, u.phone, u.avatar, u.enabled, u.createTime, u.enterpriseId, e.enterpriseName, e.enterpriseCode, e.enterpriseRole, e.status)" +
            " from SysUser u, SysEnterprise e where u.enterpriseId = e.id and" +
            "(:username is null or u.username = :username) and " +
            "(:role is null or u.role = :role) and " +
            "(:phone is null or u.phone = :phone) and " +
            "(:enabled is null or u.enabled = :enabled) and " +
            "(coalesce(:enterpriseIds, null) is null or u.enterpriseId in :enterpriseIds) and " +
            "(:enterpriseName is null or e.enterpriseName like concat('%', :enterpriseName, '%')) and " +
            "(:enterpriseCode is null or e.enterpriseCode = :enterpriseCode) and " +
            "(coalesce(:enterpriseStatus, null) is null or e.status in :enterpriseStatus)" +
            " order by u.createTime desc")
    Page<UserListVO> queryList(@Param("username") String username,
                               @Param("role") String role,
                               @Param("phone") String phone,
                               @Param("enabled") Boolean enabled,
                               @Param("enterpriseIds") List<String> enterpriseIds,
                               @Param("enterpriseName") String enterpriseName,
                               @Param("enterpriseCode") String enterpriseCode,
                               @Param("enterpriseStatus") List<String> enterpriseStatus,
                               Pageable pageable);
}
