package com.ruoyi.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.service.IProjectCustomersService;

/**
 * 项目访问权限检查工具类
 * 
 * @author evs
 * @date 2026-03-01
 */
@Component
public class ProjectAccessChecker 
{
    private static final Logger log = LoggerFactory.getLogger(ProjectAccessChecker.class);
    
    @Autowired
    private IProjectCustomersService projectCustomersService;
    
    /**
     * 检查客户是否有权访问项目
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return true=有权限，false=无权限
     */
    public boolean checkAccess(String projectId, String customerId)
    {
        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(customerId))
        {
            log.warn("权限检查失败：项目ID或客户ID为空");
            return false;
        }
        
        boolean hasAccess = projectCustomersService.checkCustomerAccess(projectId, customerId);
        
        if (!hasAccess)
        {
            log.warn("权限检查失败：客户{}无权访问项目{}", customerId, projectId);
        }
        
        return hasAccess;
    }
    
    /**
     * 检查权限，如果无权限则抛出异常
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @throws RuntimeException 无权限时抛出
     */
    public void checkAccessOrThrow(String projectId, String customerId)
    {
        if (!checkAccess(projectId, customerId))
        {
            throw new RuntimeException("无权访问该项目");
        }
    }
    
    /**
     * 检查权限，如果无权限则抛出自定义异常消息
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @param errorMessage 错误消息
     * @throws RuntimeException 无权限时抛出
     */
    public void checkAccessOrThrow(String projectId, String customerId, String errorMessage)
    {
        if (!checkAccess(projectId, customerId))
        {
            throw new RuntimeException(errorMessage);
        }
    }
}
