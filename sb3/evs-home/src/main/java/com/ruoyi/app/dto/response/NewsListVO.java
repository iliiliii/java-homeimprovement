package com.ruoyi.app.dto.response;

import java.util.List;

/**
 * 资讯列表分页VO
 */
public class NewsListVO {

    /** 资讯列表 */
    private List<NewsItemVO> list;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;

    /** 是否有更多数据 */
    private Boolean hasMore;

    public List<NewsItemVO> getList() {
        return list;
    }

    public void setList(List<NewsItemVO> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }
}
