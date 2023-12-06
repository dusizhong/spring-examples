package com.dusizhong.examples.log.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
public class SysLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String opUrl;
    private String opBiz;
    private String opBizId;
    private String opBizData;
    private String opStartTime;
    private String opEndTime;
    private String opBizResult;
    private String opUserId;
    private String opUserName;
    private String opUserIp;
    private String opUserOs;
    private String opUserBrowser;
    private String opPlatform;
    private String opTime;
}
