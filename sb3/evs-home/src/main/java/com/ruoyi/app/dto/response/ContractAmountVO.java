package com.ruoyi.app.dto.response;

import java.math.BigDecimal;

/**
 * 合同金额统计VO
 */
public class ContractAmountVO {

    /** 合同分类编码 */
    private String category;

    /** 合同分类名称 */
    private String label;

    /** 合同金额 */
    private BigDecimal amount;

    /** 附件URL */
    private String url;

    public ContractAmountVO() {
    }

    public ContractAmountVO(String category, String label, BigDecimal amount, String url) {
        this.category = category;
        this.label = label;
        this.amount = amount;
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
