package com.ruoyi.app.service;

import com.ruoyi.app.dto.response.ProjectMemberVO;
import java.util.List;

/**
 * 项目成员服务接口
 */
public interface IAppProjectMemberService {
    
    /**
     * 获取项目成员列表
     * 
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    List<ProjectMemberVO> getProjectMembers(String projectId);
}
