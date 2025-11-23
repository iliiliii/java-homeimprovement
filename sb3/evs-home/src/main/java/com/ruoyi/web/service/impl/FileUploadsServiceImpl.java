package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.FileUploadsMapper;
import com.ruoyi.web.domain.FileUploads;
import com.ruoyi.web.service.IFileUploadsService;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 文件上传Service业务层处理
 * 
 * @author evs
 * @date 2025-11-23
 */
@Service
public class FileUploadsServiceImpl implements IFileUploadsService 
{
    @Autowired
    private FileUploadsMapper fileUploadsMapper;

    /**
     * 查询文件上传
     * 
     * @param id 文件上传主键
     * @return 文件上传
     */
    @Override
    public FileUploads selectFileUploadsById(String id)
    {
        return fileUploadsMapper.selectFileUploadsById(id);
    }

    /**
     * 查询文件上传列表
     * 
     * @param fileUploads 文件上传
     * @return 文件上传
     */
    @Override
    public List<FileUploads> selectFileUploadsList(FileUploads fileUploads)
    {
        return fileUploadsMapper.selectFileUploadsList(fileUploads);
    }

    /**
     * 新增文件上传
     * 
     * @param fileUploads 文件上传
     * @return 结果
     */
    @Override
    public int insertFileUploads(FileUploads fileUploads)
    {
        // 如果 id 为空，自动生成 UUID
        if (fileUploads.getId() == null || fileUploads.getId().isEmpty()) {
            fileUploads.setId(IdUtils.fastSimpleUUID());
        }
        // 如果 size 为空，设置默认值为 0
        if (fileUploads.getSize() == null) {
            fileUploads.setSize(0L);
        }
        fileUploads.setCreatedAt(DateUtils.getNowDate());
        fileUploads.setCreatedBy(SecurityUtils.getUsername());
        return fileUploadsMapper.insertFileUploads(fileUploads);
    }

    /**
     * 修改文件上传
     * 
     * @param fileUploads 文件上传
     * @return 结果
     */
    @Override
    public int updateFileUploads(FileUploads fileUploads)
    {
        fileUploads.setUpdatedAt(DateUtils.getNowDate());
        fileUploads.setUpdatedBy(SecurityUtils.getUsername());
        return fileUploadsMapper.updateFileUploads(fileUploads);
    }

    /**
     * 批量删除文件上传
     * 
     * @param ids 需要删除的文件上传主键
     * @return 结果
     */
    @Override
    public int deleteFileUploadsByIds(String[] ids)
    {
        return fileUploadsMapper.deleteFileUploadsByIds(ids);
    }

    /**
     * 删除文件上传信息
     * 
     * @param id 文件上传主键
     * @return 结果
     */
    @Override
    public int deleteFileUploadsById(String id)
    {
        FileUploads fileUploads = fileUploadsMapper.selectFileUploadsById(id);
        if (fileUploads == null) {
            return 0;
        }
        fileUploads.setDeletedAt(DateUtils.getNowDate());
        return fileUploadsMapper.deleteFileUploadsById(id);
    }

    /**
     * 软删除文件上传信息
     * 
     * @param id 文件上传主键
     * @return 结果
     */
    @Override
    public int softDeleteFileUploadsById(String id)
    {
        FileUploads fileUploads = fileUploadsMapper.selectFileUploadsById(id);
        if (fileUploads == null) {
            return 0;
        }
        fileUploads.setDeletedAt(DateUtils.getNowDate());
        return fileUploadsMapper.updateFileUploads(fileUploads);
    }
}
