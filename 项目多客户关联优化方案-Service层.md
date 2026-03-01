# 项目多客户关联优化方案 - Service 层实现

## 3.5 创建 Service 接口

```java
package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.ProjectCustomers;

public interface IProjectCustomersService {
    
    /**
     * 查询项目的所有客户
     */
    List<ProjectCustomers> selectByProjectId(String projectId);
    
    /**
     * 查询客户的所有项目
     */
    List<ProjectCustomers> selectByCustomerId(String customerId);
    
    /**
     * 查询项目的主客户
     */
    ProjectCustomers selectPrimaryByProjectId(String projectId);
    
    /**
     * 添加项目客户关联
     * @param projectId 项目ID
     * @param customerIds 客户ID列表
     * @param primaryCustomerId 主客户ID
     * @return 成功添加的数量
     */
    int addProjectCustomers(String projectId, List<String> customerIds, String primaryCustomerId);
    
    /**
     * 移除项目客户关联
     */
    int removeProjectCustomer(String projectId, String customerId);
    
    /**
     * 设置主客户
     */
    int setPrimaryCustomer(String projectId, String customerId);
    
    /**
     * 检查客户是否有权访问项目
     */
    boolean checkCustomerAccess(String projectId, String customerId);
    
    /**
     * 更新客户角色
     */
    int updateCustomerRole(String id, String role, java.math.BigDecimal shareRatio);
}
```

## 3.6 Service 实现类

```java
package com.ruoyi.web.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.web.domain.ProjectCustomers;
import com.ruoyi.web.domain.Projects;
import com.ruoyi.web.mapper.ProjectCustomersMapper;
import com.ruoyi.web.mapper.ProjectsMapper;
import com.ruoyi.web.service.IProjectCustomersService;

@Service
public class ProjectCustomersServiceImpl implements IProjectCustomersService {
    
    @Autowired
    private ProjectCustomersMapper projectCustomersMapper;
    
    @Autowired
    private ProjectsMapper projectsMapper;
    
    // 最大客户数量限制
    private static final int MAX_CUSTOMERS_PER_PROJECT = 10;
    
    @Override
    public List<ProjectCustomers> selectByProjectId(String projectId) {
        return projectCustomersMapper.selectByProjectId(projectId);
    }
    
    @Override
    public List<ProjectCustomers> selectByCustomerId(String customerId) {
        return projectCustomersMapper.selectByCustomerId(customerId);
    }
    
    @Override
    public ProjectCustomers selectPrimaryByProjectId(String projectId) {
        return projectCustomersMapper.selectPrimaryByProjectId(projectId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addProjectCustomers(String projectId, List<String> customerIds, String primaryCustomerId) {
        // 1. 验证项目是否存在
        Projects project = projectsMapper.selectProjectsById(createProjectQuery(projectId));
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        
        // 2. 检查客户数量限制
        int currentCount = projectCustomersMapper.countByProjectId(projectId);
        if (currentCount + customerIds.size() > MAX_CUSTOMERS_PER_PROJECT) {
            throw new ServiceException("项目客户数量不能超过" + MAX_CUSTOMERS_PER_PROJECT + "个");
        }
        
        // 3. 验证主客户ID是否在列表中
        if (primaryCustomerId != null && !customerIds.contains(primaryCustomerId)) {
            throw new ServiceException("主客户必须在客户列表中");
        }
        
        // 4. 批量创建关联记录
        List<ProjectCustomers> list = new ArrayList<>();
        String currentUser = SecurityUtils.getUsername();
        Date now = new Date();
        
        for (String customerId : customerIds) {
            // 检查是否已存在
            if (projectCustomersMapper.checkCustomerInProject(projectId, customerId)) {
                continue; // 跳过已存在的关联
            }
            
            ProjectCustomers pc = new ProjectCustomers();
            pc.setId(IdUtils.fastSimpleUUID());
            pc.setProjectId(projectId);
            pc.setCustomerId(customerId);
            pc.setRole("OWNER"); // 默认角色
            pc.setIsPrimary(customerId.equals(primaryCustomerId));
            pc.setJoinDate(now);
            pc.setIsActive(true);
            pc.setCreatedAt(now);
            pc.setCreatedBy(currentUser);
            
            list.add(pc);
        }
        
        if (list.isEmpty()) {
            return 0;
        }
        
        // 5. 批量插入
        int result = projectCustomersMapper.batchInsert(list);
        
        // 6. 更新 projects 表的 customer_id（主客户）
        if (primaryCustomerId != null) {
            project.setCustomerId(primaryCustomerId);
            projectsMapper.updateProjects(project);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeProjectCustomer(String projectId, String customerId) {
        // 1. 检查是否为主客户
        ProjectCustomers primary = projectCustomersMapper.selectPrimaryByProjectId(projectId);
        if (primary != null && customerId.equals(primary.getCustomerId())) {
            throw new ServiceException("不能移除主客户，请先设置其他客户为主客户");
        }
        
        // 2. 软删除：设置为无效
        ProjectCustomers pc = new ProjectCustomers();
        pc.setIsActive(false);
        pc.setLeaveDate(new Date());
        pc.setUpdatedAt(new Date());
        pc.setUpdatedBy(SecurityUtils.getUsername());
        
        // 需要先查询ID
        List<ProjectCustomers> list = projectCustomersMapper.selectByProjectId(projectId);
        for (ProjectCustomers item : list) {
            if (customerId.equals(item.getCustomerId())) {
                pc.setId(item.getId());
                return projectCustomersMapper.update(pc);
            }
        }
        
        return 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setPrimaryCustomer(String projectId, String customerId) {
        // 1. 检查客户是否关联到项目
        if (!projectCustomersMapper.checkCustomerInProject(projectId, customerId)) {
            throw new ServiceException("该客户未关联到此项目");
        }
        
        // 2. 设置主客户
        int result = projectCustomersMapper.setPrimaryCustomer(projectId, customerId);
        
        // 3. 同步更新 projects 表的 customer_id
        Projects project = projectsMapper.selectProjectsById(createProjectQuery(projectId));
        if (project != null) {
            project.setCustomerId(customerId);
            projectsMapper.updateProjects(project);
        }
        
        return result;
    }
    
    @Override
    public boolean checkCustomerAccess(String projectId, String customerId) {
        return projectCustomersMapper.checkCustomerInProject(projectId, customerId);
    }
    
    @Override
    public int updateCustomerRole(String id, String role, java.math.BigDecimal shareRatio) {
        ProjectCustomers pc = new ProjectCustomers();
        pc.setId(id);
        pc.setRole(role);
        pc.setShareRatio(shareRatio);
        pc.setUpdatedAt(new Date());
        pc.setUpdatedBy(SecurityUtils.getUsername());
        
        return projectCustomersMapper.update(pc);
    }
    
    /**
     * 创建项目查询对象（包含权限信息）
     */
    private Projects createProjectQuery(String projectId) {
        Projects query = new Projects();
        query.setId(projectId);
        
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            query.setCurrentUserId(String.valueOf(currentUserId));
            query.setIsAdmin(SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("gly"));
        }
        
        return query;
    }
}
```

## 3.7 修改 ProjectsServiceImpl

```java
@Service
public class ProjectsServiceImpl implements IProjectsService {
    
    @Autowired
    private ProjectsMapper projectsMapper;
    
    @Autowired
    private ProjectCustomersMapper projectCustomersMapper;
    
    /**
     * 新增项目信息（支持多客户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProjects(Projects projects) {
        // 1. 生成项目ID
        if (projects.getId() == null || projects.getId().isEmpty()) {
            projects.setId(IdUtils.fastSimpleUUID());
        }
        projects.setCreatedAt(DateUtils.getNowDate());
        projects.setCreatedBy(SecurityUtils.getUsername());
        
        // 2. 插入项目基本信息
        int result = projectsMapper.insertProjects(projects);
        
        // 3. 处理客户关联（如果提供了客户列表）
        if (projects.getProjectCustomers() != null && !projects.getProjectCustomers().isEmpty()) {
            String projectId = projects.getId();
            List<ProjectCustomers> customerList = projects.getProjectCustomers();
            
            // 找出主客户
            String primaryCustomerId = null;
            for (ProjectCustomers pc : customerList) {
                if (pc.getIsPrimary() != null && pc.getIsPrimary()) {
                    primaryCustomerId = pc.getCustomerId();
                    break;
                }
            }
            
            // 如果没有指定主客户，使用第一个
            if (primaryCustomerId == null && !customerList.isEmpty()) {
                primaryCustomerId = customerList.get(0).getCustomerId();
            }
            
            // 设置 projects 表的 customer_id
            if (primaryCustomerId != null) {
                projects.setCustomerId(primaryCustomerId);
                projectsMapper.updateProjects(projects);
            }
            
            // 批量插入客户关联
            Date now = new Date();
            String currentUser = SecurityUtils.getUsername();
            
            for (ProjectCustomers pc : customerList) {
                pc.setId(IdUtils.fastSimpleUUID());
                pc.setProjectId(projectId);
                pc.setJoinDate(now);
                pc.setIsActive(true);
                pc.setCreatedAt(now);
                pc.setCreatedBy(currentUser);
                
                // 确保主客户标记正确
                if (primaryCustomerId != null && primaryCustomerId.equals(pc.getCustomerId())) {
                    pc.setIsPrimary(true);
                } else {
                    pc.setIsPrimary(false);
                }
            }
            
            projectCustomersMapper.batchInsert(customerList);
        }
        
        return result;
    }
    
    /**
     * 查询项目列表（包含客户信息）
     */
    @Override
    public List<Projects> selectProjectsListWithCustomers(Projects projects) {
        // 1. 查询项目列表
        Projects query = setCurrentUser(projects);
        List<Projects> projectsList = projectsMapper.selectProjectsList(query);
        
        if (projectsList.isEmpty()) {
            return projectsList;
        }
        
        // 2. 批量查询客户数量
        List<String> projectIds = projectsList.stream()
                .map(Projects::getId)
                .collect(Collectors.toList());
        
        List<java.util.Map<String, Object>> customerCounts = 
            projectCustomersMapper.countByProjectIds(projectIds);
        
        java.util.Map<String, Integer> countMap = customerCounts.stream()
                .collect(Collectors.toMap(
                    m -> (String) m.get("projectId"),
                    m -> ((Number) m.get("customerCount")).intValue()
                ));
        
        // 3. 为每个项目设置客户数量
        for (Projects project : projectsList) {
            Integer count = countMap.get(project.getId());
            project.setCustomerCount(count != null ? count : 0);
        }
        
        return projectsList;
    }
    
    /**
     * 查询项目详情（包含所有客户）
     */
    @Override
    public Projects selectProjectsWithAllCustomers(String id) {
        // 1. 查询项目基本信息
        Projects project = selectProjectsById(id);
        if (project == null) {
            return null;
        }
        
        // 2. 查询所有关联客户
        List<ProjectCustomers> customers = projectCustomersMapper.selectByProjectId(id);
        project.setProjectCustomers(customers);
        project.setCustomerCount(customers.size());
        
        return project;
    }
}
```

## 四、权限控制调整

### 4.1 修改 AppAuthServiceImpl

```java
/**
 * 查询用户的项目列表（支持多客户）
 */
private List<AppProjectInfo> findUserProjects(UserTypeEnum userType, String userId) {
    List<Projects> projects;
    
    if (userType == UserTypeEnum.CUSTOMER) {
        // 客户：查询 project_customers 表中 customer_id = userId 的项目
        List<ProjectCustomers> projectCustomers = 
            projectCustomersMapper.selectByCustomerId(userId);
        
        if (projectCustomers.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取项目ID列表
        List<String> projectIds = projectCustomers.stream()
                .map(ProjectCustomers::getProjectId)
                .collect(Collectors.toList());
        
        // 批量查询项目信息
        projects = appProjectMapper.selectProjectsByIds(projectIds);
    } else {
        // 员工：查询project_members中user_id = userId的项目
        projects = projectMembersService.selectProjectsByUserId(Long.parseLong(userId));
    }
    
    if (projects == null || projects.isEmpty()) {
        return new ArrayList<>();
    }
    
    return projects.stream().map(p -> {
        AppProjectInfo info = new AppProjectInfo();
        info.setId(p.getId());
        info.setCode(p.getId());
        info.setName(p.getName());
        info.setStatus(p.getStatus());
        info.setPhase(p.getStatus());
        info.setAddress(p.getAddress());
        info.setArea(p.getArea() != null ? p.getArea().doubleValue() : null);
        return info;
    }).collect(Collectors.toList());
}
```

### 4.2 修改权限验证逻辑

```java
/**
 * 验证客户是否有权访问项目（支持多客户）
 */
private Map<String, Object> validateTokenAndAccess(String token, String projectId) {
    Map<String, Object> claims = tokenManager.validateToken(extractToken(token));
    String userType = (String) claims.get("userType");
    String userId = claims.get("userId").toString();

    // 验证用户是否有权限访问该项目
    if ("customer".equals(userType)) {
        // 检查客户是否关联到该项目
        boolean hasAccess = projectCustomersMapper.checkCustomerInProject(projectId, userId);
        if (!hasAccess) {
            throw new ServiceException("无权访问该项目");
        }
    } else if ("staff".equals(userType)) {
        boolean hasAccess = dashboardMapper.checkStaffProjectAccess(userId, projectId);
        if (!hasAccess) {
            throw new ServiceException("无权访问该项目");
        }
    }

    return claims;
}
```

### 4.3 修改 AppProjectMapper

```java
@Mapper
public interface AppProjectMapper {
    
    /**
     * 根据客户ID查询项目列表（通过关联表）
     */
    @Select("SELECT DISTINCT p.id, p.project_type AS projectType, p.name, p.customer_id AS customerId, " +
            "p.description, p.address, p.area, p.budget, p.start_date AS startDate, " +
            "p.end_date AS endDate, p.status, p.priority, p.progress " +
            "FROM projects p " +
            "INNER JOIN project_customers pc ON p.id = pc.project_id " +
            "WHERE pc.customer_id = #{customerId} AND pc.is_active = 1 AND p.deleted_at IS NULL " +
            "ORDER BY pc.is_primary DESC, p.created_at DESC")
    List<Projects> selectProjectsByCustomerId(@Param("customerId") String customerId);
    
    /**
     * 根据项目ID列表批量查询
     */
    List<Projects> selectProjectsByIds(@Param("ids") List<String> ids);
}
```

### 4.4 AppProjectMapper.xml

```xml
<!-- 根据项目ID列表批量查询 -->
<select id="selectProjectsByIds" resultType="com.ruoyi.web.domain.Projects">
    SELECT id, project_type AS projectType, name, customer_id AS customerId, description,
           address, area, budget, start_date AS startDate, end_date AS endDate,
           status, priority, progress
    FROM projects
    WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    AND deleted_at IS NULL
    ORDER BY created_at DESC
</select>
```

## 五、Controller 层

### 5.1 创建 ProjectCustomersController

```java
package com.ruoyi.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.ProjectCustomers;
import com.ruoyi.web.service.IProjectCustomersService;

/**
 * 项目客户关联Controller
 */
@RestController
@RequestMapping("/evs/project-customers")
public class ProjectCustomersController extends BaseController {
    
    @Autowired
    private IProjectCustomersService projectCustomersService;
    
    /**
     * 查询项目的所有客户
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:query')")
    @GetMapping("/project/{projectId}")
    public AjaxResult getByProjectId(@PathVariable String projectId) {
        List<ProjectCustomers> list = projectCustomersService.selectByProjectId(projectId);
        return success(list);
    }
    
    /**
     * 查询客户的所有项目
     */
    @PreAuthorize("@ss.hasPermi('evs:customers:query')")
    @GetMapping("/customer/{customerId}")
    public AjaxResult getByCustomerId(@PathVariable String customerId) {
        List<ProjectCustomers> list = projectCustomersService.selectByCustomerId(customerId);
        return success(list);
    }
    
    /**
     * 添加项目客户关联
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:edit')")
    @Log(title = "项目客户关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProjectCustomersAddRequest request) {
        int result = projectCustomersService.addProjectCustomers(
            request.getProjectId(),
            request.getCustomerIds(),
            request.getPrimaryCustomerId()
        );
        return toAjax(result);
    }
    
    /**
     * 移除项目客户关联
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:edit')")
    @Log(title = "项目客户关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectId}/{customerId}")
    public AjaxResult remove(@PathVariable String projectId, @PathVariable String customerId) {
        return toAjax(projectCustomersService.removeProjectCustomer(projectId, customerId));
    }
    
    /**
     * 设置主客户
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:edit')")
    @Log(title = "设置主客户", businessType = BusinessType.UPDATE)
    @PutMapping("/primary/{projectId}/{customerId}")
    public AjaxResult setPrimary(@PathVariable String projectId, @PathVariable String customerId) {
        return toAjax(projectCustomersService.setPrimaryCustomer(projectId, customerId));
    }
    
    /**
     * 更新客户角色
     */
    @PreAuthorize("@ss.hasPermi('evs:projects:edit')")
    @Log(title = "更新客户角色", businessType = BusinessType.UPDATE)
    @PutMapping("/role")
    public AjaxResult updateRole(@RequestBody ProjectCustomersUpdateRequest request) {
        return toAjax(projectCustomersService.updateCustomerRole(
            request.getId(),
            request.getRole(),
            request.getShareRatio()
        ));
    }
}

/**
 * 添加请求对象
 */
class ProjectCustomersAddRequest {
    private String projectId;
    private List<String> customerIds;
    private String primaryCustomerId;
    
    // Getters and Setters
}

/**
 * 更新请求对象
 */
class ProjectCustomersUpdateRequest {
    private String id;
    private String role;
    private java.math.BigDecimal shareRatio;
    
    // Getters and Setters
}
```

---

**下一部分**: 前端实现方案
