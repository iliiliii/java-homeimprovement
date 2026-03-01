package com.ruoyi.web.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.mapper.ProjectCustomersMapper;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.domain.ProjectCustomers;
import com.ruoyi.web.service.IProjectsService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.exception.ServiceException;

/**
 * 项目信息Service业务层处理
 * 
 * @author evs
 * @date 2025-11-23
 */
@Service
public class ProjectsServiceImpl implements IProjectsService
{
    private static final Logger log = LoggerFactory.getLogger(ProjectsServiceImpl.class);
    
    @Autowired
    private ProjectsMapper projectsMapper;
    
    @Autowired
    private ProjectCustomersMapper projectCustomersMapper;

    /**
     * 设置当前用户权限信息
     */
    private Projects setCurrentUser(Projects projects) {
        Long currentUserId = SecurityUtils.getUserId();
        Boolean isAdmin = SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly");

        System.out.println("=== 权限设置调试 ===");
        System.out.println("SecurityUtils.getUserId(): " + currentUserId);
        System.out.println("SecurityUtils.hasRole('admin'): " + isAdmin);

        if (currentUserId != null) {
            projects.setCurrentUserId(String.valueOf(currentUserId));
            // 确保 isAdmin 始终有明确的布尔值，处理 null 的情况
            projects.setIsAdmin(isAdmin != null ? isAdmin : false);
            System.out.println("设置权限参数 - currentUserId: " + currentUserId + ", isAdmin: " + projects.getIsAdmin());
        } else {
            System.out.println("⚠️ 警告：未获取到当前用户ID");
            projects.setIsAdmin(false);
        }
        return projects;
    }

    /**
     * 查询项目信息
     *
     * @param id 项目信息主键
     * @return 项目信息
     */
    @Override
    public Projects selectProjectsById(String id)
    {
        Projects query = new Projects();
        query.setId(id);
        setCurrentUser(query);
        return projectsMapper.selectProjectsById(query);
    }

    /**
     * 查询项目信息列表
     *
     * @param projects 项目信息
     * @return 项目信息
     */
    @Override
    public List<Projects> selectProjectsList(Projects projects)
    {
        System.out.println("\n=== selectProjectsList 调试 ===");
        System.out.println("原始查询参数: " + projects);
        System.out.println("projects.getStatus(): " + projects.getStatus());

        Projects query = setCurrentUser(projects);
        System.out.println("设置权限后参数: currentUserId=" + query.getCurrentUserId() + ", isAdmin=" + query.getIsAdmin());
        System.out.println("query.getStatus(): " + query.getStatus());

        List<Projects> result = projectsMapper.selectProjectsList(query);
        System.out.println("查询结果数量: " + result.size());

        if (result.size() > 0) {
            System.out.println("返回的第一个项目: " + result.get(0));
        }

        return result;
    }

    /**
     * 新增项目信息
     * 
     * @param projects 项目信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProjects(Projects projects)
    {
        if (projects.getId() == null || projects.getId().isEmpty()) {
            projects.setId(IdUtils.fastSimpleUUID());
        }
        projects.setCreatedAt(DateUtils.getNowDate());
        projects.setCreatedBy(SecurityUtils.getUsername());
        
        // 插入项目
        int result = projectsMapper.insertProjects(projects);
        
        // 注意：不在这里自动创建客户关联
        // 客户关联由前端统一调用 addCustomersToProject 接口创建
        // 这样可以避免重复创建，逻辑更清晰
        // 
        // 如果需要向后兼容（直接调用此接口而不调用 addCustomersToProject），
        // 可以保留以下代码：
        // if (result > 0 && projects.getCustomerId() != null && !projects.getCustomerId().isEmpty()) {
        //     syncProjectCustomer(projects.getId(), projects.getCustomerId(), "insert");
        // }
        
        return result;
    }

    /**
     * 修改项目信息
     *
     * @param projects 项目信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProjects(Projects projects)
    {
        // 验证权限：检查用户是否有权限修改该项目
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        // 验证操作的项目是否存在且用户有权限
        Projects existing = selectProjectsById(projects.getId());
        if (existing == null) {
            throw new ServiceException("项目不存在或无权限操作");
        }
        
        // 检查 customer_id 是否发生变化
        String oldCustomerId = existing.getCustomerId();
        String newCustomerId = projects.getCustomerId();
        boolean customerChanged = false;
        
        if (oldCustomerId == null && newCustomerId != null) {
            customerChanged = true;
        } else if (oldCustomerId != null && !oldCustomerId.equals(newCustomerId)) {
            customerChanged = true;
        }
        
        // 更新项目
        int result = projectsMapper.updateProjects(projects);
        
        // 如果 customer_id 发生变化，同步更新 project_customers 表
        if (result > 0 && customerChanged) {
            syncProjectCustomer(projects.getId(), newCustomerId, "update", oldCustomerId);
        }
        
        return result;
    }

    /**
     * 批量删除项目信息
     *
     * @param ids 需要删除的项目信息主键
     * @return 结果
     */
    @Override
    public int deleteProjectsByIds(String[] ids)
    {
        // 验证权限：检查用户是否有权限删除这些项目
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        // 逐个验证每个ID的删除权限
        for (String id : ids) {
            Projects existing = selectProjectsById(id);
            if (existing == null) {
                throw new ServiceException("项目不存在或无权限删除: " + id);
            }
        }

        // 创建删除对象
        Projects query = new Projects();
        query.setIds(ids);
        query.setCurrentUserId(String.valueOf(currentUserId));

        return projectsMapper.deleteProjectsByIds(query);
    }

    /**
     * 删除项目信息信息
     *
     * @param id 项目信息主键
     * @return 结果
     */
    @Override
    public int deleteProjectsById(String id)
    {
        // 验证权限：检查用户是否有权限删除该项目
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户未登录");
        }

        // 验证要删除的项目是否存在且用户有权限
        Projects existing = selectProjectsById(id);
        if (existing == null) {
            throw new ServiceException("项目不存在或无权限删除");
        }

        // 创建删除对象
        Projects query = new Projects();
        query.setId(id);
        query.setCurrentUserId(String.valueOf(currentUserId));

        return projectsMapper.deleteProjectsById(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int softDeleteProjectsById(String id)
    {
        // 使用带权限控制的查询方法
        Projects projects = selectProjectsById(id);
        if (projects == null) {
            return 0;
        }
        projects.setDeletedAt(DateUtils.getNowDate());
        int result = projectsMapper.updateProjects(projects);
        
        // 同步软删除 project_customers 关联
        if (result > 0) {
            syncProjectCustomer(id, null, "delete");
        }
        
        return result;
    }
    
    /**
     * 同步 project_customers 表
     * 
     * @param projectId 项目ID
     * @param customerId 客户ID
     * @param operation 操作类型：insert, update, delete
     * @param oldCustomerId 旧客户ID（仅 update 时使用）
     */
    private void syncProjectCustomer(String projectId, String customerId, String operation, String... oldCustomerId) {
        try {
            Date now = new Date();
            String currentUser = SecurityUtils.getUsername();
            
            if ("insert".equals(operation)) {
                // 插入新项目时，创建客户关联
                if (customerId != null && !customerId.isEmpty()) {
                    // 检查是否已存在
                    boolean exists = projectCustomersMapper.checkCustomerInProject(projectId, customerId);
                    if (!exists) {
                        ProjectCustomers pc = new ProjectCustomers();
                        pc.setId(IdUtils.fastSimpleUUID());
                        pc.setProjectId(projectId);
                        pc.setCustomerId(customerId);
                        pc.setRole("OWNER");
                        pc.setIsPrimary(true);  // 设置为主客户
                        pc.setCreatedAt(now);
                        pc.setCreatedBy(currentUser);
                        
                        projectCustomersMapper.insertProjectCustomers(pc);
                        log.info("同步创建 project_customers 记录：projectId={}, customerId={}", projectId, customerId);
                    }
                }
            } else if ("update".equals(operation)) {
                // 更新项目客户时，物理删除旧关联，创建新关联
                String oldCustId = (oldCustomerId != null && oldCustomerId.length > 0) ? oldCustomerId[0] : null;
                
                // 1. 物理删除旧的主客户关联
                if (oldCustId != null && !oldCustId.isEmpty()) {
                    ProjectCustomers query = new ProjectCustomers();
                    query.setProjectId(projectId);
                    query.setCustomerId(oldCustId);
                    List<ProjectCustomers> oldList = projectCustomersMapper.selectProjectCustomersList(query);
                    
                    for (ProjectCustomers old : oldList) {
                        if (old.getIsPrimary()) {
                            projectCustomersMapper.deleteProjectCustomersById(old.getId());
                            log.info("同步物理删除旧的 project_customers 记录：id={}, customerId={}", old.getId(), oldCustId);
                        }
                    }
                }
                
                // 2. 创建新的主客户关联
                if (customerId != null && !customerId.isEmpty()) {
                    boolean exists = projectCustomersMapper.checkCustomerInProject(projectId, customerId);
                    if (!exists) {
                        ProjectCustomers pc = new ProjectCustomers();
                        pc.setId(IdUtils.fastSimpleUUID());
                        pc.setProjectId(projectId);
                        pc.setCustomerId(customerId);
                        pc.setRole("OWNER");
                        pc.setIsPrimary(true);
                        pc.setCreatedAt(now);
                        pc.setCreatedBy(currentUser);
                        
                        projectCustomersMapper.insertProjectCustomers(pc);
                        log.info("同步创建新的 project_customers 记录：projectId={}, customerId={}", projectId, customerId);
                    } else {
                        // 如果已存在，设置为主客户
                        projectCustomersMapper.clearPrimaryCustomers(projectId);
                        projectCustomersMapper.setPrimaryCustomer(projectId, customerId);
                        log.info("同步设置主客户：projectId={}, customerId={}", projectId, customerId);
                    }
                }
            } else if ("delete".equals(operation)) {
                // 物理删除项目时，物理删除所有客户关联
                List<ProjectCustomers> list = projectCustomersMapper.selectByProjectId(projectId);
                for (ProjectCustomers pc : list) {
                    if (pc.getDeletedAt() == null) {
                        projectCustomersMapper.deleteProjectCustomersById(pc.getId());
                    }
                }
                log.info("同步物理删除项目的所有 project_customers 记录：projectId={}, count={}", projectId, list.size());
            }
        } catch (Exception e) {
            log.error("同步 project_customers 失败：projectId={}, customerId={}, operation={}, error={}", 
                     projectId, customerId, operation, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public List<Projects> selectProjectsWithRelations(Projects projects, String includeRelations, String memberUserId, boolean isAdmin)
    {
        if (!StringUtils.hasText(includeRelations)) {
            return selectProjectsList(projects);
        }

        // 设置权限信息
        Projects query = setCurrentUser(projects);

        List<Projects> projectList;

        // 优先处理 customer 关联查询
        if (includeRelations.contains("customer")) {
            projectList = projectsMapper.selectProjectsWithCustomer(query);
        } else if (includeRelations.contains("projectMembers")) {
            // 通过 projectMembers 关联查询
            projectList = projectsMapper.selectProjectsWithMembers(query, memberUserId, isAdmin);
        } else {
            projectList = selectProjectsList(query);
        }

        return projectList;
    }

    @Override
    public Projects selectProjectsWithRelationsById(String id, String includeRelations)
    {
        if (!StringUtils.hasText(includeRelations)) {
            return selectProjectsById(id);
        }

        Projects project;

        if (includeRelations.contains("customer")) {
            Projects query = new Projects();
            query.setId(id);
            setCurrentUser(query);
            project = projectsMapper.selectProjectsWithCustomerById(query);
        } else {
            project = selectProjectsById(id);
        }

        return project;
    }

    @Override
    public List<Projects> selectProjectsListWithScheduleInfo(Projects projects, boolean includeCustomer)
    {
        System.out.println("\n=== selectProjectsListWithScheduleInfo 调试 ===");
        System.out.println("接收到的参数 projects: " + projects);
        System.out.println("projects.getStatus(): " + projects.getStatus());
        System.out.println("includeCustomer: " + includeCustomer);

        // 设置权限信息（自动应用权限控制）
        Projects query = setCurrentUser(projects);

        System.out.println("设置权限后 query: " + query);
        System.out.println("query.getStatus(): " + query.getStatus());
        System.out.println("query.getCurrentUserId(): " + query.getCurrentUserId());
        System.out.println("query.getIsAdmin(): " + query.getIsAdmin());

        // 1. 查询项目列表（带权限过滤）
        List<Projects> projectsList;
        if (includeCustomer) {
            // 查询项目列表（包含客户信息）
            projectsList = projectsMapper.selectProjectsWithCustomer(query);
        } else {
            // 查询项目列表（不包含客户信息）
            projectsList = projectsMapper.selectProjectsList(query);
        }

        // 2. 如果无项目，直接返回
        if (projectsList.isEmpty()) {
            return projectsList;
        }

        // 3. 获取所有项目ID（保持String类型）
        List<String> projectIds = projectsList.stream()
                .map(Projects::getId)
                .collect(Collectors.toList());

        // 4. 批量查询进度统计
        Map<String, Map<String, Object>> statsMap = projectsMapper.selectScheduleStatsMap(projectIds);

        // 5. 为每个项目设置统计信息
        for (Projects project : projectsList) {
            Map<String, Object> stats = statsMap.get(project.getId());
            if (stats != null) {
                project.setTotalSchedules(((Number) stats.get("total_count")).longValue());
                project.setCompletedSchedules(((Number) stats.get("completed_count")).longValue());
                project.setInProgressSchedules(((Number) stats.get("in_progress_count")).longValue());
                project.setProgressRate(((Number) stats.get("progress_rate")).intValue());
            } else {
                // 如果没有进度数据，初始化为0
                project.setTotalSchedules(0L);
                project.setCompletedSchedules(0L);
                project.setInProgressSchedules(0L);
                project.setProgressRate(0);
            }
        }

        return projectsList;
    }
}
