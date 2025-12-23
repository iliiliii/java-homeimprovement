package com.ruoyi.app.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.app.dto.response.NewsItemVO;
import com.ruoyi.app.dto.response.NewsListVO;
import com.ruoyi.app.mapper.AppNewsMapper;
import com.ruoyi.app.service.IAppNewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小程序资讯服务实现
 */
@Service
public class AppNewsServiceImpl implements IAppNewsService {

    private static final Logger log = LoggerFactory.getLogger(AppNewsServiceImpl.class);

    @Autowired
    private AppNewsMapper newsMapper;

    @Override
    public List<NewsItemVO> getBannerNews() {
        log.debug("[AppNews] 获取Banner资讯列表");
        return newsMapper.selectNewsByPosition("banner");
    }

    @Override
    public NewsListVO getNewsList(String position, Integer pageNum, Integer pageSize) {
        log.debug("[AppNews] 获取资讯列表: position={}, pageNum={}, pageSize={}", position, pageNum, pageSize);
        
        // 使用PageHelper进行分页
        PageHelper.startPage(pageNum, pageSize);
        List<NewsItemVO> list = newsMapper.selectNewsByPosition(position);
        PageInfo<NewsItemVO> pageInfo = new PageInfo<>(list);
        
        // 构建返回对象
        NewsListVO result = new NewsListVO();
        result.setList(pageInfo.getList());
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setHasMore(pageInfo.getPageNum() < pageInfo.getPages());
        
        return result;
    }
}
