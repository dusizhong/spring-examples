package com.dusizhong.examples.log.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
public class SysUser implements Serializable {

    @Id
    private String id;
    private String username;
    @JsonIgnore
    private String password;
    private String role;
    private String phone;
    private Boolean credentialsNonExpired = true;
    private Boolean accountNonExpired = true;
    private Boolean accountNonLocked = true;
    private Boolean enabled = true;
    private String createTime;
}
