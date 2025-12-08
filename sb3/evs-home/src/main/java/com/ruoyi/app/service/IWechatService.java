package com.ruoyi.app.service;

/**
 * 微信小程序服务接口
 */
public interface IWechatService {
    
    /**
     * 微信登录会话信息
     */
    class WxSession {
        private String openId;
        private String unionId;
        private String sessionKey;
        
        public String getOpenId() { return openId; }
        public void setOpenId(String openId) { this.openId = openId; }
        public String getUnionId() { return unionId; }
        public void setUnionId(String unionId) { this.unionId = unionId; }
        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
    }
    
    /**
     * 微信手机号信息
     */
    class WxPhoneInfo {
        private String phoneNumber;
        private String purePhoneNumber;
        private String countryCode;
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getPurePhoneNumber() { return purePhoneNumber; }
        public void setPurePhoneNumber(String purePhoneNumber) { this.purePhoneNumber = purePhoneNumber; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }
    
    /**
     * 通过code获取微信会话信息（openId, sessionKey）
     * 
     * @param code 微信登录凭证
     * @return 会话信息
     */
    WxSession code2Session(String code);
    
    /**
     * 通过phoneCode获取手机号
     * 
     * @param phoneCode 手机号动态令牌
     * @return 手机号信息
     */
    WxPhoneInfo getPhoneNumber(String phoneCode);
    
    /**
     * 解密微信加密数据
     * 
     * @param encryptedData 加密数据
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 解密后的数据
     */
    String decryptData(String encryptedData, String sessionKey, String iv);
}
