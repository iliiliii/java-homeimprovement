package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.NewsConsultationMapper;
import com.ruoyi.web.domain.NewsConsultation;
import com.ruoyi.web.service.INewsConsultationService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 新闻咨询设置Service业务层处理
 * 
 * @author evs
 * @date 2025-12-08
 */
@Service
public class NewsConsultationServiceImpl implements INewsConsultationService 
{
    @Autowired
    private NewsConsultationMapper newsConsultationMapper;

    /**
     * 查询新闻咨询设置
     * 
     * @param id 新闻咨询设置主键
     * @return 新闻咨询设置
     */
    @Override
    public NewsConsultation selectNewsConsultationById(String id)
    {
        return newsConsultationMapper.selectNewsConsultationById(id);
    }

    /**
     * 查询新闻咨询设置列表
     * 
     * @param newsConsultation 新闻咨询设置
     * @return 新闻咨询设置
     */
    @Override
    public List<NewsConsultation> selectNewsConsultationList(NewsConsultation newsConsultation)
    {
        return newsConsultationMapper.selectNewsConsultationList(newsConsultation);
    }

    /**
     * 新增新闻咨询设置
     * 
     * @param newsConsultation 新闻咨询设置
     * @return 结果
     */
    @Override
    public int insertNewsConsultation(NewsConsultation newsConsultation)
    {
        if (newsConsultation.getId() == null || newsConsultation.getId().isEmpty()) {
            newsConsultation.setId(IdUtils.fastSimpleUUID());
        }
        newsConsultation.setCreatedAt(DateUtils.getNowDate());
        newsConsultation.setCreatedBy(SecurityUtils.getUsername());
        return newsConsultationMapper.insertNewsConsultation(newsConsultation);
    }

    /**
     * 修改新闻咨询设置
     * 
     * @param newsConsultation 新闻咨询设置
     * @return 结果
     */
    @Override
    public int updateNewsConsultation(NewsConsultation newsConsultation)
    {
        newsConsultation.setUpdatedAt(DateUtils.getNowDate());
        newsConsultation.setUpdatedBy(SecurityUtils.getUsername());
        return newsConsultationMapper.updateNewsConsultation(newsConsultation);
    }

    /**
     * 批量删除新闻咨询设置
     * 
     * @param ids 需要删除的新闻咨询设置主键
     * @return 结果
     */
    @Override
    public int deleteNewsConsultationByIds(String[] ids)
    {
        return newsConsultationMapper.deleteNewsConsultationByIds(ids);
    }

    /**
     * 删除新闻咨询设置信息
     * 
     * @param id 新闻咨询设置主键
     * @return 结果
     */
    @Override
    public int deleteNewsConsultationById(String id)
    {
        return newsConsultationMapper.deleteNewsConsultationById(id);
    }
}
