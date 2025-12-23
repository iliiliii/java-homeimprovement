package com.ruoyi.app.mapper;

import com.ruoyi.app.dto.response.NewsItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序资讯数据Mapper
 */
@Mapper
public interface AppNewsMapper {

    /**
     * 根据发布位置查询资讯列表
     * @param position 发布位置：banner/home/commercial
     * @return 资讯列表
     */
    List<NewsItemVO> selectNewsByPosition(@Param("position") String position);

    /**
     * 根据发布位置统计资讯数量
     * @param position 发布位置
     * @return 数量
     */
    Long countNewsByPosition(@Param("position") String position);
}
