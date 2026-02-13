package com.ruoyi.app.enums;

/**
 * 用户类型枚举
 */
public enum UserTypeEnum {
    
    CUSTOMER("customer", "客户"),
    STAFF("staff", "员工"),
    GUEST("guest", "游客");
    
    private final String code;
    private final String desc;
    
    UserTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static UserTypeEnum fromCode(String code) {
        for (UserTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
