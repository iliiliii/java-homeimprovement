package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.FileUploadsMapper;
import com.ruoyi.web.domain.FileUploads;
import com.ruoyi.web.service.IFileUploadsService;

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
        return fileUploadsMapper.deleteFileUploadsById(id);
    }
}
