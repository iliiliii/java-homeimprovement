package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AppWechatBindings;

/**
 * 微信绑定Service接口
 * 
 * @author evs
 * @date 2026-01-19
 */
public interface IAppWechatBindingsService 
{
    /**
     * 查询微信绑定
     * 
     * @param id 微信绑定主键
     * @return 微信绑定
     */
    public AppWechatBindings selectAppWechatBindingsById(String id);

    /**
     * 查询微信绑定列表
     * 
     * @param appWechatBindings 微信绑定
     * @return 微信绑定集合
     */
    public List<AppWechatBindings> selectAppWechatBindingsList(AppWechatBindings appWechatBindings);

    /**
     * 新增微信绑定
     * 
     * @param appWechatBindings 微信绑定
     * @return 结果
     */
    public int insertAppWechatBindings(AppWechatBindings appWechatBindings);

    /**
     * 修改微信绑定
     * 
     * @param appWechatBindings 微信绑定
     * @return 结果
     */
    public int updateAppWechatBindings(AppWechatBindings appWechatBindings);

    /**
     * 批量删除微信绑定
     * 
     * @param ids 需要删除的微信绑定主键集合
     * @return 结果
     */
    public int deleteAppWechatBindingsByIds(String[] ids);

    /**
     * 删除微信绑定信息
     * 
     * @param id 微信绑定主键
     * @return 结果
     */
    public int deleteAppWechatBindingsById(String id);
    
    /**
     * 根据openId查询微信绑定信息
     * 
     * @param openId 微信openId
     * @return 微信绑定信息
     */
    public AppWechatBindings selectAppWechatBindingsByOpenId(String openId);
    
    /**
     * 根据userId删除微信绑定信息
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteAppWechatBindingsByUserId(String userId);
    
    /**
     * 根据手机号查询微信绑定信息
     * 
     * @param phone 手机号
     * @return 微信绑定信息
     */
    public AppWechatBindings selectAppWechatBindingsByPhone(String phone);
    
    /**
     * 更新最后登录时间
     * 
     * @param openId 微信openId
     * @return 结果
     */
    public int updateLastLoginTime(String openId);
}
