package com.ruoyi.web.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.mapper.UserPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户岗位关联接口
 */
@RestController
@RequestMapping("/evs/userPost")
public class UserPostController extends BaseController {

    @Autowired
    private UserPostMapper userPostMapper;

    /**
     * 获取所有用户-岗位关联
     */
    @GetMapping("/all")
    public AjaxResult getAllUserPost() {
        List<Map<String, Object>> list = userPostMapper.selectUserPostAll();
        return success(list);
    }

    /**
     * 根据岗位ID获取用户列表
     */
    @GetMapping("/users/{postId}")
    public AjaxResult getUsersByPostId(@PathVariable Long postId) {
        List<Map<String, Object>> list = userPostMapper.selectUsersByPostId(postId);
        return success(list);
    }
}
