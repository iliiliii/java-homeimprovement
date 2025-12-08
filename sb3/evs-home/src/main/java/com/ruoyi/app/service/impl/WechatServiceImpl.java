package com.ruoyi.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.app.service.IWechatService;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 微信小程序服务实现
 */
@Service
public class WechatServiceImpl implements IWechatService {
    
    private static final Logger log = LoggerFactory.getLogger(WechatServiceImpl.class);
    
    @Value("${app.wechat.appid:}")
    private String appId;
    
    @Value("${app.wechat.secret:}")
    private String appSecret;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public WxSession code2Session(String code) {
        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            throw new ServiceException("微信小程序配置未完成，请联系管理员");
        }
        
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, appSecret, code
        );
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            log.info("微信code2session响应: {}", response);
            
            JSONObject json = JSON.parseObject(response);
            
            // 检查错误
            if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                String errMsg = json.getString("errmsg");
                log.error("微信登录失败: {}", errMsg);
                throw new ServiceException("微信登录失败: " + errMsg);
            }
            
            WxSession session = new WxSession();
            session.setOpenId(json.getString("openid"));
            session.setUnionId(json.getString("unionid"));
            session.setSessionKey(json.getString("session_key"));
            
            return session;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信API异常", e);
            throw new ServiceException("微信登录服务异常，请稍后重试");
        }
    }
    
    @Override
    public WxPhoneInfo getPhoneNumber(String phoneCode) {
        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            throw new ServiceException("微信小程序配置未完成，请联系管理员");
        }
        
        // 先获取access_token
        String accessToken = getAccessToken();
        
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("code", phoneCode);
            
            String response = restTemplate.postForObject(url, requestBody.toJSONString(), String.class);
            log.info("微信获取手机号响应: {}", response);
            
            JSONObject json = JSON.parseObject(response);
            
            // 检查错误
            if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                String errMsg = json.getString("errmsg");
                log.error("获取手机号失败: {}", errMsg);
                throw new ServiceException("获取手机号失败: " + errMsg);
            }
            
            JSONObject phoneInfo = json.getJSONObject("phone_info");
            if (phoneInfo == null) {
                throw new ServiceException("获取手机号失败：返回数据异常");
            }
            
            WxPhoneInfo result = new WxPhoneInfo();
            result.setPhoneNumber(phoneInfo.getString("phoneNumber"));
            result.setPurePhoneNumber(phoneInfo.getString("purePhoneNumber"));
            result.setCountryCode(phoneInfo.getString("countryCode"));
            
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取微信手机号异常", e);
            throw new ServiceException("获取手机号服务异常，请稍后重试");
        }
    }
    
    /**
     * 获取微信access_token
     */
    private String getAccessToken() {
        String url = String.format(
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
            appId, appSecret
        );
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = JSON.parseObject(response);
            
            if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                String errMsg = json.getString("errmsg");
                log.error("获取access_token失败: {}", errMsg);
                throw new ServiceException("微信服务异常: " + errMsg);
            }
            
            return json.getString("access_token");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取access_token异常", e);
            throw new ServiceException("微信服务异常，请稍后重试");
        }
    }
    
    @Override
    public String decryptData(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
            
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            byte[] decrypted = cipher.doFinal(dataBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密微信数据失败", e);
            throw new ServiceException("数据解密失败");
        }
    }
}
