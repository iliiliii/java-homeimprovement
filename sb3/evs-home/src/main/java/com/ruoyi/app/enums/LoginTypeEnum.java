package com.ruoyi.app.enums;

/**
 * 登录类型枚举
 */
public enum LoginTypeEnum {
    
    WECHAT("wechat", "微信登录"),
    SMS("sms", "短信验证码登录"),
    PASSWORD("password", "密码登录");
    
    private final String code;
    private final String desc;
    
    LoginTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static LoginTypeEnum fromCode(String code) {
        for (LoginTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
