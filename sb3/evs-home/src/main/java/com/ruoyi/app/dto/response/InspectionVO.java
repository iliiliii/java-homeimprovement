package com.ruoyi.app.dto.response;

import java.util.Date;
import java.util.List;

/**
 * 质检记录VO
 */
public class InspectionVO {

    /** 质检ID */
    private String id;

    /** 质检类型 */
    private String inspectionType;

    /** 质检类型显示文本 */
    private String inspectionTypeText;

    /** 质检标题 */
    private String title;

    /** 质检描述 */
    private String description;

    /** 质检结果：PASS-合格, FAIL-不合格 */
    private String result;

    /** 结果显示文本 */
    private String resultText;

    /** 检查日期 */
    private Date inspectionDate;

    /** 质检图片列表 */
    private List<String> images;

    /** 备注 */
    private String remarks;

    /** 关联问题数量 */
    private int issueCount;

    /** 关联问题列表 */
    private List<IssueVO> issues;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    public String getInspectionTypeText() {
        return inspectionTypeText;
    }

    public void setInspectionTypeText(String inspectionTypeText) {
        this.inspectionTypeText = inspectionTypeText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public Date getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Date inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(int issueCount) {
        this.issueCount = issueCount;
    }

    public List<IssueVO> getIssues() {
        return issues;
    }

    public void setIssues(List<IssueVO> issues) {
        this.issues = issues;
    }
}
