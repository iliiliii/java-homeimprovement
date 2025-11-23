package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.FileUploads;

/**
 * 文件上传Mapper接口
 * 
 * @author evs
 * @date 2025-11-23
 */
public interface FileUploadsMapper 
{
    /**
     * 查询文件上传
     * 
     * @param id 文件上传主键
     * @return 文件上传
     */
    public FileUploads selectFileUploadsById(String id);

    /**
     * 查询文件上传列表
     * 
     * @param fileUploads 文件上传
     * @return 文件上传集合
     */
    public List<FileUploads> selectFileUploadsList(FileUploads fileUploads);

    /**
     * 新增文件上传
     * 
     * @param fileUploads 文件上传
     * @return 结果
     */
    public int insertFileUploads(FileUploads fileUploads);

    /**
     * 修改文件上传
     * 
     * @param fileUploads 文件上传
     * @return 结果
     */
    public int updateFileUploads(FileUploads fileUploads);

    /**
     * 删除文件上传
     * 
     * @param id 文件上传主键
     * @return 结果
     */
    public int deleteFileUploadsById(String id);

    /**
     * 批量删除文件上传
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFileUploadsByIds(String[] ids);

    /**
     * 软删除文件上传信息
     * 
     * @param id 文件上传主键
     * @return 结果
     */
    public int softDeleteFileUploadsById(String id);
}
