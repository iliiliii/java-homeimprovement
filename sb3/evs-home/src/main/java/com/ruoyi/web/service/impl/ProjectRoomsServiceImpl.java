package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.ProjectRoomsMapper;
import com.ruoyi.web.domain.ProjectRooms;
import com.ruoyi.web.service.IProjectRoomsService;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 项目房间Service业务层处理
 * 
 * @author evs
 * @date 2025-11-27
 */
@Service
public class ProjectRoomsServiceImpl implements IProjectRoomsService 
{
    @Autowired
    private ProjectRoomsMapper projectRoomsMapper;

    /**
     * 查询项目房间
     * 
     * @param id 项目房间主键
     * @return 项目房间
     */
    @Override
    public ProjectRooms selectProjectRoomsById(String id)
    {
        return projectRoomsMapper.selectProjectRoomsById(id);
    }

    /**
     * 查询项目房间列表
     * 
     * @param projectRooms 项目房间
     * @return 项目房间
     */
    @Override
    public List<ProjectRooms> selectProjectRoomsList(ProjectRooms projectRooms)
    {
        return projectRoomsMapper.selectProjectRoomsList(projectRooms);
    }

    /**
     * 新增项目房间
     * 
     * @param projectRooms 项目房间
     * @return 结果
     */
    @Override
    public int insertProjectRooms(ProjectRooms projectRooms)
    {
        // 如果 id 为空，自动生成 UUID
        if (projectRooms.getId() == null || projectRooms.getId().isEmpty()) {
            projectRooms.setId(IdUtils.fastSimpleUUID());
        }
        projectRooms.setCreatedAt(DateUtils.getNowDate());
        projectRooms.setCreatedBy(SecurityUtils.getUsername());
        return projectRoomsMapper.insertProjectRooms(projectRooms);
    }

    /**
     * 修改项目房间
     * 
     * @param projectRooms 项目房间
     * @return 结果
     */
    @Override
    public int updateProjectRooms(ProjectRooms projectRooms)
    {
        projectRooms.setUpdatedAt(DateUtils.getNowDate());
        projectRooms.setUpdatedBy(SecurityUtils.getUsername());
        return projectRoomsMapper.updateProjectRooms(projectRooms);
    }

    /**
     * 批量删除项目房间
     * 
     * @param ids 需要删除的项目房间主键
     * @return 结果
     */
    @Override
    public int deleteProjectRoomsByIds(String[] ids)
    {
        return projectRoomsMapper.deleteProjectRoomsByIds(ids);
    }

    /**
     * 删除项目房间信息
     * 
     * @param id 项目房间主键
     * @return 结果
     */
    @Override
    public int deleteProjectRoomsById(String id)
    {
        return projectRoomsMapper.deleteProjectRoomsById(id);
    }
}
