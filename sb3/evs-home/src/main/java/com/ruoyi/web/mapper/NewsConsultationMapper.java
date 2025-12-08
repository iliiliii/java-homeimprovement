package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.NewsConsultation;

/**
 * 新闻咨询设置Mapper接口
 * 
 * @author evs
 * @date 2025-12-08
 */
public interface NewsConsultationMapper 
{
    /**
     * 查询新闻咨询设置
     * 
     * @param id 新闻咨询设置主键
     * @return 新闻咨询设置
     */
    public NewsConsultation selectNewsConsultationById(String id);

    /**
     * 查询新闻咨询设置列表
     * 
     * @param newsConsultation 新闻咨询设置
     * @return 新闻咨询设置集合
     */
    public List<NewsConsultation> selectNewsConsultationList(NewsConsultation newsConsultation);

    /**
     * 新增新闻咨询设置
     * 
     * @param newsConsultation 新闻咨询设置
     * @return 结果
     */
    public int insertNewsConsultation(NewsConsultation newsConsultation);

    /**
     * 修改新闻咨询设置
     * 
     * @param newsConsultation 新闻咨询设置
     * @return 结果
     */
    public int updateNewsConsultation(NewsConsultation newsConsultation);

    /**
     * 删除新闻咨询设置
     * 
     * @param id 新闻咨询设置主键
     * @return 结果
     */
    public int deleteNewsConsultationById(String id);

    /**
     * 批量删除新闻咨询设置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNewsConsultationByIds(String[] ids);
}
