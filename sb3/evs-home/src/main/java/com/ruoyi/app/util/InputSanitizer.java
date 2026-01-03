package com.ruoyi.app.util;

import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * 输入数据清理工具类
 * 用于防止XSS攻击、SQL注入等安全问题
 */
public class InputSanitizer {

    // SQL注入关键字正则
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(\\b(select|insert|update|delete|drop|truncate|alter|create|exec|execute|union|declare|cast|convert|xp_|sp_|0x)\\b|--|;|/\\*|\\*/)",
            Pattern.CASE_INSENSITIVE
    );

    // XSS攻击关键字正则
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script[^>]*>.*?</script>|<iframe[^>]*>.*?</iframe>|javascript:|vbscript:|on\\w+\\s*=|expression\\s*\\()",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // 危险字符正则
    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile(
            "[<>\"'&;\\\\]"
    );

    /**
     * 清理文本输入（标题、名称等短文本）
     * 移除HTML标签，转义特殊字符
     */
    public static String sanitizeText(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        // 去除首尾空格
        String result = input.trim();
        // HTML转义
        result = HtmlUtils.htmlEscape(result);
        return result;
    }

    /**
     * 清理大文本输入（描述、内容等）
     * 保留基本格式，但移除危险内容
     */
    public static String sanitizeContent(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        String result = input.trim();
        // 移除script标签和事件处理器
        result = result.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        result = result.replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "");
        result = result.replaceAll("(?i)javascript:", "");
        result = result.replaceAll("(?i)vbscript:", "");
        result = result.replaceAll("(?i)on\\w+\\s*=", "");
        // HTML转义
        result = HtmlUtils.htmlEscape(result);
        return result;
    }

    /**
     * 检查是否包含SQL注入风险
     */
    public static boolean containsSqlInjection(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * 检查是否包含XSS攻击风险
     */
    public static boolean containsXss(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * 检查是否包含危险字符
     */
    public static boolean containsDangerousChars(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return DANGEROUS_CHARS_PATTERN.matcher(input).find();
    }

    /**
     * 验证并清理文本，如果包含危险内容则抛出异常
     */
    public static String validateAndSanitizeText(String input, String fieldName) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        // 检查SQL注入
        if (containsSqlInjection(input)) {
            throw new IllegalArgumentException(fieldName + "包含非法字符");
        }
        
        // 检查XSS
        if (containsXss(input)) {
            throw new IllegalArgumentException(fieldName + "包含非法内容");
        }
        
        return sanitizeText(input);
    }

    /**
     * 验证并清理大文本内容
     */
    public static String validateAndSanitizeContent(String input, String fieldName) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        // 检查SQL注入
        if (containsSqlInjection(input)) {
            throw new IllegalArgumentException(fieldName + "包含非法字符");
        }
        
        return sanitizeContent(input);
    }

    /**
     * 验证ID格式（只允许字母、数字、下划线、横线）
     */
    public static boolean isValidId(String id) {
        if (StringUtils.isEmpty(id)) {
            return false;
        }
        return id.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * 验证JSON数组格式（简单验证）
     */
    public static boolean isValidJsonArray(String json) {
        if (StringUtils.isEmpty(json)) {
            return true; // 空值允许
        }
        String trimmed = json.trim();
        return trimmed.startsWith("[") && trimmed.endsWith("]");
    }

    /**
     * 清理JSON字符串中的危险内容
     */
    public static String sanitizeJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        // 移除可能的XSS内容
        String result = json.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        result = result.replaceAll("(?i)javascript:", "");
        return result;
    }

    /**
     * 验证文件路径（防止路径遍历攻击）
     */
    public static boolean isValidFilePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        // 不允许路径遍历
        if (path.contains("..") || path.contains("./") || path.contains("/.")) {
            return false;
        }
        // 不允许绝对路径
        if (path.startsWith("/") || path.matches("^[a-zA-Z]:.*")) {
            return false;
        }
        return true;
    }

    /**
     * 限制字符串长度
     */
    public static String limitLength(String input, int maxLength) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        if (input.length() > maxLength) {
            return input.substring(0, maxLength);
        }
        return input;
    }
}
