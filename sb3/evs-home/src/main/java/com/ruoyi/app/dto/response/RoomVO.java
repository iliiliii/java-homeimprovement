package com.ruoyi.app.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间信息VO（小程序端）
 */
public class RoomVO {
    
    /** 房间ID */
    private String id;
    
    /** 房间名称 */
    private String roomName;
    
    /** 房间类型 */
    private String roomType;
    
    /** 房间类型文本 */
    private String roomTypeText;
    
    /** 房间面积 */
    private BigDecimal area;
    
    /** 房间描述 */
    private String description;
    
    /** 楼层 */
    private String floor;
    
    /** 朝向 */
    private String orientation;
    
    /** 朝向文本 */
    private String orientationText;
    
    /** 设计图数量 */
    private int imageCount;
    
    /** 设计图URL列表 */
    private List<String> images;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomTypeText() {
        return roomTypeText;
    }

    public void setRoomTypeText(String roomTypeText) {
        this.roomTypeText = roomTypeText;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getOrientationText() {
        return orientationText;
    }

    public void setOrientationText(String orientationText) {
        this.orientationText = orientationText;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
