package com.ruoyi.app.service;

import java.util.List;

/**
 * 游客配置服务接口
 * 用于管理游客用户可访问的演示项目
 */
public interface IGuestConfigService {
    
    /**
     * 获取游客演示项目ID列表
     * 从字典 guest_demo 中获取所有项目ID
     * 
     * @return 演示项目ID列表
     */
    List<String> getGuestDemoProjectIds();
    
    /**
     * 检查项目ID是否为游客演示项目
     * 
     * @param projectId 项目ID
     * @return true-是演示项目，false-不是
     */
    boolean isGuestDemoProject(String projectId);
    
    /**
     * 验证游客用户是否有权访问指定项目
     * 游客只能访问演示项目
     * 
     * @param projectId 项目ID
     * @return true-有权限，false-无权限
     */
    boolean validateGuestProjectAccess(String projectId);
    
    /**
     * 获取默认的游客演示项目ID
     * 返回 projects_01 对应的项目ID
     * 
     * @return 默认演示项目ID，如果不存在返回null
     */
    String getDefaultGuestProjectId();
}
