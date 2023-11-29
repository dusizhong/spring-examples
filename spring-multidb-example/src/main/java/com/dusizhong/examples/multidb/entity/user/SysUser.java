package com.dusizhong.examples.multidb.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
public class SysUser implements Serializable {

    @Id
    private String id;
    private String username;
    @JsonIgnore
    private String password;
    private String createTime;
}
