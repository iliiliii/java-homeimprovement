package com.ruoyi.app.dto.response;

/**
 * 项目信息
 */
public class AppProjectInfo {
    
    /** 项目ID */
    private String id;
    
    /** 项目编号 */
    private String code;
    
    /** 项目名称 */
    private String name;
    
    /** 项目状态 */
    private String status;
    
    /** 项目阶段 */
    private String phase;
    
    /** 项目地址 */
    private String address;
    
    /** 面积 */
    private Double area;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPhase() {
        return phase;
    }
    
    public void setPhase(String phase) {
        this.phase = phase;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Double getArea() {
        return area;
    }
    
    public void setArea(Double area) {
        this.area = area;
    }
}
