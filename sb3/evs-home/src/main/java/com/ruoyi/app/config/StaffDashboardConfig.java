package com.ruoyi.app.config;

import com.ruoyi.app.mapper.AppDashboardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工首页配置
 * 所有参数从字典表读取，由后端完全控制
 * 
 * 字典配置说明：
 * 1. decoration_staff_dashboard - 基础配置
 *    - dict_label='page_size', dict_value=数值 (每页数据量，默认100)
 *    - dict_label='enable_paging', dict_value='true'/'false' (是否启用分页)
 *    
 * 2. decoration_status_project_order - 状态排序和筛选
 *    - dict_value=状态值(如IN_PROGRESS), dict_sort=排序顺序
 *    - status='0' 表示该状态允许显示，status='1' 表示该状态不显示
 */
@Component
public class StaffDashboardConfig {

    private static final String DICT_TYPE_DASHBOARD = "decoration_staff_dashboard";
    private static final String DICT_TYPE_STATUS_ORDER = "decoration_status_project_order";

    @Autowired
    private AppDashboardMapper dashboardMapper;

    /**
     * 获取每页数据量
     * 从字典 decoration_staff_dashboard 中 dict_label='page_size' 的 dict_value 读取
     * 默认值：100
     */
    public int getPageSize() {
        String value = dashboardMapper.selectDictValueByLabel(DICT_TYPE_DASHBOARD, "page_size");
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // 解析失败，返回默认值
            }
        }
        return 100;
    }

    /**
     * 是否启用分页
     * 从字典 decoration_staff_dashboard 中 dict_label='enable_paging' 的 dict_value 读取
     * 默认值：true
     */
    public boolean isEnablePaging() {
        String value = dashboardMapper.selectDictValueByLabel(DICT_TYPE_DASHBOARD, "enable_paging");
        if (value != null && !value.isEmpty()) {
            return "true".equalsIgnoreCase(value) || "1".equals(value);
        }
        return true;
    }

    /**
     * 获取允许显示的状态列表
     * 从字典 decoration_status_project_order 读取，status='0' 的状态才显示
     * 返回 null 表示显示所有状态
     */
    public List<String> getAllowedStatuses() {
        List<Map<String, Object>> statusList = dashboardMapper.selectAllowedProjectStatuses();
        if (statusList == null || statusList.isEmpty()) {
            return null; // 返回null表示不筛选，显示所有状态
        }
        return statusList.stream()
                .map(m -> (String) m.get("dictValue"))
                .filter(v -> v != null && !v.isEmpty())
                .collect(Collectors.toList());
    }
}
