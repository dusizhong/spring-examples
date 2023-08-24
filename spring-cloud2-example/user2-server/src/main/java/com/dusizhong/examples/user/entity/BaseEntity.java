package com.dusizhong.examples.user.entity;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.SqlUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

/**
 * 公共Entity
 * @author Dusizhong
 * @since 2022-09-21
 */
@Slf4j
@Data
@MappedSuperclass
public class BaseEntity {

    @Id
    private String id;
    @Column(updatable = false)
    private String createUser;
    @Column(updatable = false)
    private String createTime;
    private String updateUser;
    private String updateTime;
    private String status;
    private String remark;

    @Transient
    private Integer pageNumber;
    @Transient
    private Integer pageSize;
    @Transient
    private String pageSort;

    //2023-04-03 弃用 发现同步保存获取保存后id时有时候为空
    @PrePersist
    public void prePersist() {
        JSONObject user = Oauth2Utils.getCurrentUser();
        //this.id = SqlUtils.createId();
        this.createUser = user.getString("id");
        this.createTime = SqlUtils.getDateTime();
    }

    @PreUpdate
    public void preUpdate() {
        JSONObject user = Oauth2Utils.getCurrentUser();
        this.updateUser = user.getString("id");
        this.updateTime = SqlUtils.getDateTime();
    }
//
//    @PostLoad
//    public void postLoad() {
//    }
//
//    @PreRemove
//    public void preRemove() {
//        // throw new RuntimeException("发生异常！删除操作将回滚");
//    }
//
//    //记录删除日志
//    @PostRemove
//    public void postRemove() {
//        String removeUser = Oauth2Utils.getCurrentUser().getString("id");
//        String removeTime = SqlUtils.getDateTime();
//        String removeData = JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("removeUser", removeUser);
//        jsonObject.put("removeTime", removeTime);
//        jsonObject.put("removeEntity", this.getClass());
//        jsonObject.put("removeData", removeData);
//        log.info("postRemove: {}", jsonObject.toJSONString());
//    }
}
