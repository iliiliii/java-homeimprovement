package com.ruoyi.app.service.impl;

import com.ruoyi.app.service.IGuestConfigService;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游客配置服务实现
 */
@Service
public class GuestConfigServiceImpl implements IGuestConfigService {
    
    private static final Logger log = LoggerFactory.getLogger(GuestConfigServiceImpl.class);
    
    /**
     * 游客演示字典类型
     */
    private static final String GUEST_DEMO_DICT_TYPE = "guest_demo";
    
    /**
     * 默认项目key
     */
    private static final String DEFAULT_PROJECT_KEY = "projects_01";
    
    @Override
    public List<String> getGuestDemoProjectIds() {
        try {
            List<SysDictData> dictDataList = DictUtils.getDictCache(GUEST_DEMO_DICT_TYPE);
            
            if (dictDataList == null || dictDataList.isEmpty()) {
                log.warn("[游客配置] 未找到游客演示项目配置，字典类型: {}", GUEST_DEMO_DICT_TYPE);
                return new ArrayList<>();
            }
            
            // 提取所有以 projects_ 开头的字典项的值（项目ID）
            List<String> projectIds = dictDataList.stream()
                    .filter(dict -> dict.getDictLabel() != null && dict.getDictLabel().startsWith("projects_"))
                    .map(SysDictData::getDictValue)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
            
            log.info("[游客配置] 获取到 {} 个演示项目ID: {}", projectIds.size(), projectIds);
            return projectIds;
            
        } catch (Exception e) {
            log.error("[游客配置] 获取游客演示项目ID列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean isGuestDemoProject(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            return false;
        }
        
        List<String> demoProjectIds = getGuestDemoProjectIds();
        boolean isDemo = demoProjectIds.contains(projectId);
        
        log.debug("[游客配置] 检查项目 {} 是否为演示项目: {}", projectId, isDemo);
        return isDemo;
    }
    
    @Override
    public boolean validateGuestProjectAccess(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            log.warn("[游客配置] 项目ID为空，拒绝访问");
            return false;
        }
        
        boolean hasAccess = isGuestDemoProject(projectId);
        
        if (!hasAccess) {
            log.warn("[游客配置] 游客尝试访问非演示项目: {}", projectId);
        }
        
        return hasAccess;
    }
    
    @Override
    public String getDefaultGuestProjectId() {
        try {
            List<SysDictData> dictDataList = DictUtils.getDictCache(GUEST_DEMO_DICT_TYPE);
            
            if (dictDataList == null || dictDataList.isEmpty()) {
                log.warn("[游客配置] 未找到游客演示项目配置");
                return null;
            }
            
            // 查找 projects_01 对应的项目ID
            String defaultProjectId = dictDataList.stream()
                    .filter(dict -> DEFAULT_PROJECT_KEY.equals(dict.getDictLabel()))
                    .map(SysDictData::getDictValue)
                    .filter(StringUtils::isNotEmpty)
                    .findFirst()
                    .orElse(null);
            
            if (defaultProjectId == null) {
                log.warn("[游客配置] 未找到默认演示项目 ({})", DEFAULT_PROJECT_KEY);
                // 如果没有 projects_01，返回第一个可用的项目ID
                defaultProjectId = getGuestDemoProjectIds().stream()
                        .findFirst()
                        .orElse(null);
            }
            
            log.info("[游客配置] 默认演示项目ID: {}", defaultProjectId);
            return defaultProjectId;
            
        } catch (Exception e) {
            log.error("[游客配置] 获取默认演示项目ID失败", e);
            return null;
        }
    }
}
