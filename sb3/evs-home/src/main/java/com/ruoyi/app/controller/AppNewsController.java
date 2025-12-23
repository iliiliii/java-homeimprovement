package com.ruoyi.app.controller;

import com.ruoyi.app.dto.response.NewsItemVO;
import com.ruoyi.app.dto.response.NewsListVO;
import com.ruoyi.app.service.IAppNewsService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序资讯接口
 */
@RestController
@RequestMapping("/app/news")
public class AppNewsController {

    private static final Logger log = LoggerFactory.getLogger(AppNewsController.class);

    @Autowired
    private IAppNewsService newsService;

    /**
     * 获取资讯列表
     * @param position 发布位置：banner/home/commercial
     * @param pageNum 页码（banner类型时忽略）
     * @param pageSize 每页数量（banner类型时忽略）
     */
    @GetMapping("/list")
    public AjaxResult getNewsList(
            @RequestParam String position,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 参数校验
            if (position == null || position.isEmpty()) {
                return AjaxResult.error(400, "position参数不能为空");
            }
            
            // banner类型不分页
            if ("banner".equals(position)) {
                List<NewsItemVO> list = newsService.getBannerNews();
                return AjaxResult.success(list);
            }
            
            // home/commercial类型分页
            if (!"home".equals(position) && !"commercial".equals(position)) {
                return AjaxResult.error(400, "position参数值无效，支持：banner/home/commercial");
            }
            
            NewsListVO result = newsService.getNewsList(position, pageNum, pageSize);
            return AjaxResult.success(result);
            
        } catch (ServiceException e) {
            log.warn("获取资讯列表失败: {}", e.getMessage());
            return AjaxResult.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
        } catch (Exception e) {
            log.error("获取资讯列表异常", e);
            return AjaxResult.error(500, "获取数据失败: " + e.getMessage());
        }
    }
}
