package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.AppWechatBindingsMapper;
import com.ruoyi.system.domain.AppWechatBindings;
import com.ruoyi.system.service.IAppWechatBindingsService;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 微信绑定Service业务层处理
 * 
 * @author evs
 * @date 2026-01-19
 */
@Service
public class AppWechatBindingsServiceImpl implements IAppWechatBindingsService 
{
    @Autowired
    private AppWechatBindingsMapper appWechatBindingsMapper;

    /**
     * 查询微信绑定
     * 
     * @param id 微信绑定主键
     * @return 微信绑定
     */
    @Override
    public AppWechatBindings selectAppWechatBindingsById(String id)
    {
        return appWechatBindingsMapper.selectAppWechatBindingsById(id);
    }

    /**
     * 查询微信绑定列表
     * 
     * @param appWechatBindings 微信绑定
     * @return 微信绑定
     */
    @Override
    public List<AppWechatBindings> selectAppWechatBindingsList(AppWechatBindings appWechatBindings)
    {
        return appWechatBindingsMapper.selectAppWechatBindingsList(appWechatBindings);
    }

    /**
     * 新增微信绑定
     * 
     * @param appWechatBindings 微信绑定
     * @return 结果
     */
    @Override
    public int insertAppWechatBindings(AppWechatBindings appWechatBindings)
    {
        // 如果 id 为空，自动生成 UUID
        if (appWechatBindings.getId() == null || appWechatBindings.getId().isEmpty()) {
            appWechatBindings.setId(IdUtils.fastSimpleUUID());
        }
        appWechatBindings.setCreateTime(DateUtils.getNowDate());
        return appWechatBindingsMapper.insertAppWechatBindings(appWechatBindings);
    }

    /**
     * 修改微信绑定
     * 
     * @param appWechatBindings 微信绑定
     * @return 结果
     */
    @Override
    public int updateAppWechatBindings(AppWechatBindings appWechatBindings)
    {
        return appWechatBindingsMapper.updateAppWechatBindings(appWechatBindings);
    }

    /**
     * 批量删除微信绑定
     * 
     * @param ids 需要删除的微信绑定主键
     * @return 结果
     */
    @Override
    public int deleteAppWechatBindingsByIds(String[] ids)
    {
        return appWechatBindingsMapper.deleteAppWechatBindingsByIds(ids);
    }

    /**
     * 删除微信绑定信息
     * 
     * @param id 微信绑定主键
     * @return 结果
     */
    @Override
    public int deleteAppWechatBindingsById(String id)
    {
        return appWechatBindingsMapper.deleteAppWechatBindingsById(id);
    }
    
    /**
     * 根据openId查询微信绑定信息
     * 
     * @param openId 微信openId
     * @return 微信绑定信息
     */
    @Override
    public AppWechatBindings selectAppWechatBindingsByOpenId(String openId)
    {
        AppWechatBindings query = new AppWechatBindings();
        query.setOpenId(openId);
        List<AppWechatBindings> list = appWechatBindingsMapper.selectAppWechatBindingsList(query);
        return list.isEmpty() ? null : list.get(0);
    }
    
    /**
     * 根据手机号查询微信绑定信息
     * 
     * @param phone 手机号
     * @return 微信绑定信息
     */
    @Override
    public AppWechatBindings selectAppWechatBindingsByPhone(String phone)
    {
        AppWechatBindings query = new AppWechatBindings();
        query.setPhone(phone);
        List<AppWechatBindings> list = appWechatBindingsMapper.selectAppWechatBindingsList(query);
        return list.isEmpty() ? null : list.get(0);
    }
    
    /**
     * 更新最后登录时间
     * 
     * @param openId 微信openId
     * @return 结果
     */
    @Override
    public int updateLastLoginTime(String openId)
    {
        AppWechatBindings binding = selectAppWechatBindingsByOpenId(openId);
        if (binding != null) {
            binding.setLastLoginTime(DateUtils.getNowDate());
            return appWechatBindingsMapper.updateAppWechatBindings(binding);
        }
        return 0;
    }
}
