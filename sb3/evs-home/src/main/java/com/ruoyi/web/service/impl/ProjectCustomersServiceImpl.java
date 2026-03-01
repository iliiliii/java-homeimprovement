package com.ruoyi.web.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.web.mapper.ProjectCustomersMapper;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.domain.ProjectCustomers;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.service.IProjectCustomersService;

/**
 * 项目客户Service业务层处理
 * 
 * @author evs
 * @date 2026-03-01
 */
@Service
public class ProjectCustomersServiceImpl implements IProjectCustomersService 
{
    private static final Logger log = LoggerFactory.getLogger(ProjectCustomersServiceImpl.class);
    
    @Autowired
    private ProjectCustomersMapper projectCustomersMapper;
    
    @Autowired
    private ProjectsMapper projectsMapper;

    /**
     * 查询项目客户
     * 
     * @param id 项目客户主键
     * @return 项目客户
     */
    @Override
    public ProjectCustomers selectProjectCustomersById(String id)
    {
        return projectCustomersMapper.selectProjectCustomersById(id);
    }

    /**
     * 查询项目客户列表
     * 
     * @param projectCustomers 项目客户
     * @return 项目客户
     */
    @Override
    public List<ProjectCustomers> selectProjectCustomersList(ProjectCustomers projectCustomers)
    {
        return projectCustomersMapper.selectProjectCustomersList(projectCustomers);
    }

    /**
     * 新增项目客户
     * 
     * @param projectCustomers 项目客户
     * @return 结果
     */
    @Override
    public int insertProjectCustomers(ProjectCustomers projectCustomers)
    {
        return projectCustomersMapper.insertProjectCustomers(projectCustomers);
    }

    /**
     * 修改项目客户
     * 
     * @param projectCustomers 项目客户
     * @return 结果
     */
    @Override
    public int updateProjectCustomers(ProjectCustomers projectCustomers)
    {
        return projectCustomersMapper.updateProjectCustomers(projectCustomers);
    }

    /**
     * 批量删除项目客户
     * 
     * @param ids 需要删除的项目客户主键
     * @return 结果
     */
    @Override
    public int deleteProjectCustomersByIds(String[] ids)
    {
        return projectCustomersMapper.deleteProjectCustomersByIds(ids);
    }

    /**
     * 删除项目客户信息
     * 
     * @param id 项目客户主键
     * @return 结果
     */
    @Override
    public int deleteProjectCustomersById(String id)
    {
        return projectCustomersMapper.deleteProjectCustomersById(id);
    }
    
    /**
     * 查询项目的所有客户
     * 
     * @param projectId 项目ID
     * @return 客户列表（包含客户详细信息）
     */
    @Override
    public List<ProjectCustomers> selectByProjectId(String projectId)
    {
        if (StringUtils.isEmpty(projectId))
        {
            log.warn("查询项目客户失败：项目ID为空");
            return new ArrayList<>();
        }
        return projectCustomersMapper.selectByProjectId(projectId);
    }
    
    /**
     * 查询客户的所有项目
     * 
     * @param customerId 客户ID
     * @return 项目关联列表
     */
    @Override
    public List<ProjectCustomers> selectByCustomerId(String customerId)
    {
        if (StringUtils.isEmpty(customerId))
        {
            log.warn("查询客户项目失败：客户ID为空");
            return new ArrayList<>();
        }
        return projectCustomersMapper.selectByCustomerId(customerId);
    }
    
    /**
     * 查询项目的主客户
     * 
     * @param projectId 项目ID
     * @return 主客户信息
     */
    @Override
    public ProjectCustomers selectPrimaryByProjectId(String projectId)
    {
        if (StringUtils.isEmpty(projectId))
        {
            log.warn("查询主客户失败：项目ID为空");
            return null;
        }
        return projectCustomersMapper.selectPrimaryByProjectId(projectId);
    }
    
    /**
     * 添加客户到项目（支持多个客户）
     * 
     * @param projectId 项目ID
     * @param customerIds 客户ID列表
     * @param primaryCustomerId 主客户ID（可选）
     * @return 成功添加的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addProjectCustomers(String projectId, List<String> customerIds, String primaryCustomerId)
    {
        // 参数验证
        if (StringUtils.isEmpty(projectId))
        {
            log.error("添加项目客户失败：项目ID为空");
            throw new RuntimeException("项目ID不能为空");
        }
        
        if (customerIds == null || customerIds.isEmpty())
        {
            log.error("添加项目客户失败：客户ID列表为空");
            throw new RuntimeException("客户ID列表不能为空");
        }
        
        // 验证客户数量限制（最多10个）
        int currentCount = projectCustomersMapper.countByProjectId(projectId);
        if (currentCount + customerIds.size() > 10)
        {
            log.error("添加项目客户失败：超过最大客户数量限制（10个），当前{}个，尝试添加{}个", 
                     currentCount, customerIds.size());
            throw new RuntimeException("项目客户数量不能超过10个");
        }
        
        // 构建批量插入数据
        List<ProjectCustomers> list = new ArrayList<>();
        Date now = new Date();
        
        for (String customerId : customerIds)
        {
            if (StringUtils.isEmpty(customerId))
            {
                continue;
            }
            
            // 检查是否已存在（未删除的记录）
            boolean exists = projectCustomersMapper.checkCustomerInProject(projectId, customerId);
            if (exists)
            {
                log.warn("客户{}已关联到项目{}，跳过", customerId, projectId);
                continue;
            }
            
            // 不再需要检查软删除记录，因为移除时使用物理删除
            
            ProjectCustomers pc = new ProjectCustomers();
            pc.setId(IdUtils.fastSimpleUUID());
            pc.setProjectId(projectId);
            pc.setCustomerId(customerId);
            pc.setRole("OWNER");
            pc.setIsPrimary(customerId.equals(primaryCustomerId));
            pc.setCreatedAt(now);
            
            list.add(pc);
        }
        
        if (list.isEmpty())
        {
            log.warn("没有需要添加的客户");
            return 0;
        }
        
        // 批量插入
        int result = projectCustomersMapper.batchInsert(list);
        log.info("成功添加{}个客户到项目{}", result, projectId);
        
        return result;
    }
    
    /**
     * 移除客户（物理删除）
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeProjectCustomer(String projectId, String customerId)
    {
        // 参数验证
        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(customerId))
        {
            log.error("移除项目客户失败：项目ID或客户ID为空");
            throw new RuntimeException("项目ID和客户ID不能为空");
        }
        
        // 检查是否存在
        boolean exists = projectCustomersMapper.checkCustomerInProject(projectId, customerId);
        if (!exists)
        {
            log.warn("客户{}未关联到项目{}，无需移除", customerId, projectId);
            return 0;
        }
        
        // 检查是否是主客户
        ProjectCustomers primary = projectCustomersMapper.selectPrimaryByProjectId(projectId);
        if (primary != null && customerId.equals(primary.getCustomerId()))
        {
            log.error("移除项目客户失败：不能移除主客户");
            throw new RuntimeException("不能移除主客户，请先设置其他客户为主客户");
        }
        
        // 查询关联ID
        ProjectCustomers query = new ProjectCustomers();
        query.setProjectId(projectId);
        query.setCustomerId(customerId);
        List<ProjectCustomers> list = projectCustomersMapper.selectProjectCustomersList(query);
        
        if (list.isEmpty())
        {
            log.warn("未找到项目{}的客户{}关联记录", projectId, customerId);
            return 0;
        }
        
        // 物理删除（不是软删除）
        int result = projectCustomersMapper.deleteProjectCustomersById(list.get(0).getId());
        log.info("成功从项目{}移除客户{}（物理删除）", projectId, customerId);
        
        return result;
    }
    
    /**
     * 设置主客户
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setPrimaryCustomer(String projectId, String customerId)
    {
        // 参数验证
        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(customerId))
        {
            log.error("设置主客户失败：项目ID或客户ID为空");
            throw new RuntimeException("项目ID和客户ID不能为空");
        }
        
        // 检查客户是否关联到项目
        boolean exists = projectCustomersMapper.checkCustomerInProject(projectId, customerId);
        if (!exists)
        {
            log.error("设置主客户失败：客户{}未关联到项目{}", customerId, projectId);
            throw new RuntimeException("该客户未关联到项目");
        }
        
        // 1. 先清除所有主客户标记
        projectCustomersMapper.clearPrimaryCustomers(projectId);
        
        // 2. 设置新的主客户
        int result = projectCustomersMapper.setPrimaryCustomer(projectId, customerId);
        
        // 同步更新 projects.customer_id（保持数据一致性）
        try {
            Projects project = new Projects();
            project.setId(projectId);
            project.setCustomerId(customerId);
            projectsMapper.updateProjects(project);
            log.info("同步更新 projects.customer_id 成功：projectId={}, customerId={}", projectId, customerId);
        } catch (Exception e) {
            log.error("同步更新 projects.customer_id 失败：{}", e.getMessage(), e);
            // 不抛出异常，因为触发器会处理同步
            // 这里只是作为双重保险
        }
        
        log.info("成功设置项目{}的主客户为{}", projectId, customerId);
        
        return result;
    }
    
    /**
     * 检查客户是否有权访问项目
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @return true=有权限，false=无权限
     */
    @Override
    public boolean checkCustomerAccess(String projectId, String customerId)
    {
        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(customerId))
        {
            log.warn("权限检查失败：项目ID或客户ID为空");
            return false;
        }
        
        return projectCustomersMapper.checkCustomerInProject(projectId, customerId);
    }
}
