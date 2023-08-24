package com.dusizhong.examples.jpa.entity;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.jpa.util.SqlUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;

@Slf4j
@Data
@MappedSuperclass
public class BaseEntity {

    @Id
    private String id;
    @Column(updatable = false)
    private String createTime;
    @Column(updatable = false)
    private String createUser;
    private String updateTime;
    private String updateUser;
    private String status;
    private String remark;

    @Transient
    private Integer pageNum;
    @Transient
    private Integer pageSize;

    @PrePersist
    public void prePersist() {
        this.id = StringUtils.isEmpty(this.id) ? SqlUtils.createId() : this.id;
        //this.createUser = Oauth2Utils.getCurrentUser().getString("id");;
        this.createTime = SqlUtils.getDateTime();
        this.status = StringUtils.isEmpty(this.status) ? "EDIT" : this.status;
    }

    @PreUpdate
    public void preUpdate() {
        //this.updateUser = Oauth2Utils.getCurrentUser().getString("id");
        this.updateTime = SqlUtils.getDateTime();
    }

    @PostLoad
    public void postLoad() {
    }

    @PreRemove
    public void preRemove() {
        // throw new RuntimeException("发生异常！删除操作将回滚");
    }

    //记录删除日志
    @PostRemove
    public void postRemove() {
        //String removeData = JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
        String removeData = JSONObject.toJSONString(this);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("removeEntity", this.getClass());
        jsonObject.put("removeTime", SqlUtils.getDateTime());
        //jsonObject.put("removeUser", Oauth2Utils.getCurrentUser().getString("id"));
        jsonObject.put("removeData", removeData);
        log.info("postRemove: {}", jsonObject.toJSONString());
    }
}
