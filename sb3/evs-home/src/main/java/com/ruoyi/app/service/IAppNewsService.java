package com.ruoyi.app.service;

import com.ruoyi.app.dto.response.NewsItemVO;
import com.ruoyi.app.dto.response.NewsListVO;

import java.util.List;

/**
 * 小程序资讯服务接口
 */
public interface IAppNewsService {

    /**
     * 获取Banner资讯列表（不分页）
     * @return 资讯列表
     */
    List<NewsItemVO> getBannerNews();

    /**
     * 获取资讯列表（分页）
     * @param position 发布位置：home/commercial
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页资讯列表
     */
    NewsListVO getNewsList(String position, Integer pageNum, Integer pageSize);
}
