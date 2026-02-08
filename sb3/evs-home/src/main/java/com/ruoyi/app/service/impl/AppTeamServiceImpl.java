package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.response.TeamMemberVO;
import com.ruoyi.app.mapper.AppUserMapper;
import com.ruoyi.app.service.IAppTeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 团队成员服务实现
 */
@Service
public class AppTeamServiceImpl implements IAppTeamService {
    
    private static final Logger log = LoggerFactory.getLogger(AppTeamServiceImpl.class);
    
    @Autowired
    private AppUserMapper userMapper;
    
    @Override
    public List<TeamMemberVO> getTeamMembers() {
        try {
            // 查询符合条件的用户（包含岗位信息）：email不为空、未删除、未停用
            List<Map<String, Object>> users = userMapper.selectTeamMembersWithPost();
            
            // 转换为VO
            List<TeamMemberVO> teamMembers = new ArrayList<>();
            for (Map<String, Object> user : users) {
                TeamMemberVO vo = new TeamMemberVO();
                
                // 姓名：优先使用昵称，否则使用用户名
                String nickName = (String) user.get("nickName");
                String userName = (String) user.get("userName");
                vo.setName(nickName != null && !nickName.isEmpty() ? nickName : userName);
                
                // 岗位：使用关联的岗位名称，如果没有岗位则显示"团队成员"
                String postNames = (String) user.get("postNames");
                vo.setPost(postNames != null && !postNames.isEmpty() ? postNames : "团队成员");
                
                // 头像
                vo.setAvatar((String) user.get("avatar"));
                
                teamMembers.add(vo);
            }
            
            return teamMembers;
        } catch (Exception e) {
            log.error("获取团队成员列表失败", e);
            return new ArrayList<>();
        }
    }
}
