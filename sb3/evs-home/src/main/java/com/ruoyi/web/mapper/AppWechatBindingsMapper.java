package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AppWechatBindings;

/**
 * 微信绑定Mapper接口
 * 
 * @author evs
 * @date 2026-01-19
 */
public interface AppWechatBindingsMapper 
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
     * 删除微信绑定
     * 
     * @param id 微信绑定主键
     * @return 结果
     */
    public int deleteAppWechatBindingsById(String id);

    /**
     * 批量删除微信绑定
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAppWechatBindingsByIds(String[] ids);
}
