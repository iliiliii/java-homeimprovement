package com.ruoyi.app.security;

import com.ruoyi.app.enums.UserTypeEnum;
import java.util.List;

/**
 * 小程序用户上下文
 * 使用ThreadLocal存储当前请求的用户信息
 */
public class AppContext {
    
    private static final ThreadLocal<AppUserContext> CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置用户上下文
     */
    public static void setContext(AppUserContext userContext) {
        CONTEXT.set(userContext);
    }
    
    /**
     * 获取用户上下文
     */
    public static AppUserContext getContext() {
        return CONTEXT.get();
    }
    
    /**
     * 清除用户上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }
    
    /**
     * 获取用户ID
     */
    public static String getUserId() {
        AppUserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }
    
    /**
     * 获取用户类型
     */
    public static UserTypeEnum getUserType() {
        AppUserContext context = getContext();
        return context != null ? context.getUserType() : null;
    }
    
    /**
     * 获取用户手机号
     */
    public static String getPhone() {
        AppUserContext context = getContext();
        return context != null ? context.getPhone() : null;
    }
    
    /**
     * 获取用户姓名
     */
    public static String getName() {
        AppUserContext context = getContext();
        return context != null ? context.getName() : null;
    }
    
    /**
     * 获取项目ID列表
     */
    public static List<String> getProjectIds() {
        AppUserContext context = getContext();
        return context != null ? context.getProjectIds() : null;
    }
    
    /**
     * 获取设备ID
     */
    public static String getDeviceId() {
        AppUserContext context = getContext();
        return context != null ? context.getDeviceId() : null;
    }
    
    /**
     * 是否客户
     */
    public static boolean isCustomer() {
        return UserTypeEnum.CUSTOMER.equals(getUserType());
    }
    
    /**
     * 是否员工
     */
    public static boolean isStaff() {
        return UserTypeEnum.STAFF.equals(getUserType());
    }
    
    /**
     * 是否游客
     */
    public static boolean isGuest() {
        return UserTypeEnum.GUEST.equals(getUserType());
    }
    
    /**
     * 检查是否有项目权限
     */
    public static boolean hasProjectPermission(String projectId) {
        List<String> projectIds = getProjectIds();
        return projectIds != null && projectIds.contains(projectId);
    }
    
    /**
     * 用户上下文数据
     */
    public static class AppUserContext {
        private String userId;
        private UserTypeEnum userType;
        private String phone;
        private String name;
        private List<String> projectIds;
        private String deviceId;
        private String token;
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public UserTypeEnum getUserType() {
            return userType;
        }
        
        public void setUserType(UserTypeEnum userType) {
            this.userType = userType;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public List<String> getProjectIds() {
            return projectIds;
        }
        
        public void setProjectIds(List<String> projectIds) {
            this.projectIds = projectIds;
        }
        
        public String getDeviceId() {
            return deviceId;
        }
        
        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
    }
}
