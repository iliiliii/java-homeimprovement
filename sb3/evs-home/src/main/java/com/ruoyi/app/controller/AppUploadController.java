package com.ruoyi.app.controller;

import com.ruoyi.app.security.AppTokenManager;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小程序文件上传接口
 */
@RestController
@RequestMapping("/app")
public class AppUploadController {

    private static final Logger log = LoggerFactory.getLogger(AppUploadController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private AppTokenManager tokenManager;

    /**
     * 小程序上传文件
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("file") MultipartFile file) {
        try {
            // 验证Token
            if (token == null || token.isEmpty()) {
                return AjaxResult.error(401, "未提供认证Token");
            }
            
            // 提取并验证Token
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            try {
                tokenManager.validateToken(actualToken);
            } catch (Exception e) {
                log.warn("Token验证失败: {}", e.getMessage());
                return AjaxResult.error(401, "认证失败");
            }

            if (file == null || file.isEmpty()) {
                return AjaxResult.error("上传文件不能为空");
            }

            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            
            log.info("小程序上传文件成功: {}", fileName);
            return ajax;
        } catch (Exception e) {
            log.error("小程序上传文件失败", e);
            return AjaxResult.error("上传失败: " + e.getMessage());
        }
    }
}
