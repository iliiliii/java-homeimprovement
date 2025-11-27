package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.ProjectRooms;

/**
 * 项目房间Mapper接口
 * 
 * @author evs
 * @date 2025-11-27
 */
public interface ProjectRoomsMapper 
{
    /**
     * 查询项目房间
     * 
     * @param id 项目房间主键
     * @return 项目房间
     */
    public ProjectRooms selectProjectRoomsById(String id);

    /**
     * 查询项目房间列表
     * 
     * @param projectRooms 项目房间
     * @return 项目房间集合
     */
    public List<ProjectRooms> selectProjectRoomsList(ProjectRooms projectRooms);

    /**
     * 新增项目房间
     * 
     * @param projectRooms 项目房间
     * @return 结果
     */
    public int insertProjectRooms(ProjectRooms projectRooms);

    /**
     * 修改项目房间
     * 
     * @param projectRooms 项目房间
     * @return 结果
     */
    public int updateProjectRooms(ProjectRooms projectRooms);

    /**
     * 删除项目房间
     * 
     * @param id 项目房间主键
     * @return 结果
     */
    public int deleteProjectRoomsById(String id);

    /**
     * 批量删除项目房间
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectRoomsByIds(String[] ids);
}
