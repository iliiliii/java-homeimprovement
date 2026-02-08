package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.response.ProjectMemberVO;
import com.ruoyi.app.mapper.AppUserMapper;
import com.ruoyi.app.service.IAppProjectMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目成员服务实现
 */
@Service
public class AppProjectMemberServiceImpl implements IAppProjectMemberService {
    
    private static final Logger log = LoggerFactory.getLogger(AppProjectMemberServiceImpl.class);
    
    @Autowired
    private AppUserMapper userMapper;
    
    @Override
    public List<ProjectMemberVO> getProjectMembers(String projectId) {
        try {
            if (projectId == null || projectId.isEmpty()) {
                log.warn("项目ID为空");
                return new ArrayList<>();
            }
            
            // 查询项目成员（包含用户信息和岗位）
            List<Map<String, Object>> members = userMapper.selectProjectMembers(projectId);
            
            // 转换为VO
            List<ProjectMemberVO> projectMembers = new ArrayList<>();
            for (Map<String, Object> member : members) {
                ProjectMemberVO vo = new ProjectMemberVO();
                
                vo.setId((String) member.get("id"));
                vo.setUserId(String.valueOf(member.get("userId")));
                
                // 姓名：优先使用昵称，否则使用用户名
                String nickName = (String) member.get("nickName");
                String userName = (String) member.get("userName");
                vo.setName(nickName != null && !nickName.isEmpty() ? nickName : userName);
                
                // 岗位：使用关联的岗位名称，如果没有岗位则显示"团队成员"
                String postNames = (String) member.get("postNames");
                vo.setPost(postNames != null && !postNames.isEmpty() ? postNames : "团队成员");
                
                // 头像
                vo.setAvatar((String) member.get("avatar"));
                
                // 项目角色
                vo.setRole((String) member.get("role"));
                
                projectMembers.add(vo);
            }
            
            return projectMembers;
        } catch (Exception e) {
            log.error("获取项目成员列表失败: projectId={}", projectId, e);
            return new ArrayList<>();
        }
    }
}
