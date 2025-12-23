package com.ruoyi.app.dto.response;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 资讯列表项VO
 */
public class NewsItemVO {

    /** 资讯ID */
    private String id;

    /** 标题 */
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 封面图片URL */
    private String coverImage;

    /** 跳转地址 */
    private String jumpUrl;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date publishTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
}
