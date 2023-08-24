package com.dusizhong.examples.cloud.user.repository;

import com.dusizhong.examples.cloud.user.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysUserRepository extends JpaRepository<SysUser, String>, JpaSpecificationExecutor<SysUser> {

    SysUser findByUsername(String username);
    SysUser findByPhone(String phone);
    SysUser findByIdCardNo(String phone);

//    @Query("select new com.dusizhong.examples.cloud.user.model.UserListVO" +
//            "(u.id, u.username, u.role, u.phone, u.avatar, u.enabled, u.createTime, u.enterpriseId, e.enterpriseName, e.enterpriseCode, e.enterpriseRole, e.status)" +
//            " from SysUser u left join SysEnterprise e on u.enterpriseId = e.id where" +
//            "(:username is null or u.username = :username)" +
//            " and (:role is null or u.role like concat('%', :role, '%'))" +
//            " and (:phone is null or u.phone = :phone)" +
//            " and (coalesce(:enterpriseIds, null) is null or u.enterpriseId in :enterpriseIds)" +
//            " and (:enterpriseName is null or e.enterpriseName like concat('%', :enterpriseName, '%'))" +
//            " and (:enterpriseCode is null or e.enterpriseCode = :enterpriseCode)" +
//            " and (:enterpriseStatus is null or e.status = :enterpriseStatus)" +
//            " order by u.createTime desc")
//    Page<UserListVO> queryList(String username, String role, String phone, List<String> enterpriseIds, String enterpriseName, String enterpriseCode, String enterpriseStatus, Pageable pageable);
}
