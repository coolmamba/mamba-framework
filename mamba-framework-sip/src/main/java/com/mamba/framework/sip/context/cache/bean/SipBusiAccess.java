package com.mamba.framework.sip.context.cache.bean;

import java.io.Serializable;

public class SipBusiAccess implements Serializable {
    private Integer busiAccessId;

    private String busiCode;

    private Integer accessChannel;

    private String serviceClassName;

    private String serviceMethodName;

    private String remark;

    private Integer entityId;

    private String state;

    public Integer getBusiAccessId() {
        return busiAccessId;
    }

    public void setBusiAccessId(Integer busiAccessId) {
        this.busiAccessId = busiAccessId;
    }

    public String getBusiCode() {
        return busiCode;
    }

    public void setBusiCode(String busiCode) {
        this.busiCode = busiCode;
    }

    public Integer getAccessChannel() {
        return accessChannel;
    }

    public void setAccessChannel(Integer accessChannel) {
        this.accessChannel = accessChannel;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public void setServiceClassName(String serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    public String getServiceMethodName() {
        return serviceMethodName;
    }

    public void setServiceMethodName(String serviceMethodName) {
        this.serviceMethodName = serviceMethodName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
