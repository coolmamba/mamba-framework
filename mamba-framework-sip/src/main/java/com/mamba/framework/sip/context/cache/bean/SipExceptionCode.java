package com.mamba.framework.sip.context.cache.bean;

public class SipExceptionCode {
    private String exceptionKey;

    private Integer accessChannel;

    private String code;

    private String desc;

    private String state;

    public String getExceptionKey() {
        return exceptionKey;
    }

    public void setExceptionKey(String exceptionKey) {
        this.exceptionKey = exceptionKey;
    }

    public Integer getAccessChannel() {
        return accessChannel;
    }

    public void setAccessChannel(Integer accessChannel) {
        this.accessChannel = accessChannel;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
