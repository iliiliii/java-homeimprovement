package com.ruoyi.app.service;

import com.ruoyi.app.dto.response.TeamMemberVO;
import java.util.List;

/**
 * 团队成员服务接口
 */
public interface IAppTeamService {
    
    /**
     * 获取团队成员列表
     * 过滤条件：email不为空、未删除、未停用
     * 
     * @return 团队成员列表
     */
    List<TeamMemberVO> getTeamMembers();
}
