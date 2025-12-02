/**
 * API 接口定义
 */
import { get, post } from '@/utils/request'

// ==================== 用户相关 ====================

/**
 * 用户登录
 * @param {Object} data - { phone, projectCode }
 */
export const login = (data) => {
  return post('/api/client/login', data)
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  return get('/api/client/userInfo')
}

// ==================== 项目相关 ====================

/**
 * 获取项目详情
 */
export const getProjectDetail = () => {
  return get('/api/client/project/detail')
}

/**
 * 获取项目进度
 */
export const getProjectProgress = () => {
  return get('/api/client/project/progress')
}

// ==================== 施工排期 ====================

/**
 * 获取施工阶段列表
 */
export const getPhaseList = () => {
  return get('/api/client/schedule/phases')
}

/**
 * 获取阶段详情
 * @param {Number} phaseId - 阶段ID
 */
export const getPhaseDetail = (phaseId) => {
  return get(`/api/client/schedule/phase/${phaseId}`)
}

// ==================== 设计方案 ====================

/**
 * 获取设计图列表
 * @param {Object} params - { space, page, pageSize }
 */
export const getDesignList = (params) => {
  return get('/api/client/design/list', params)
}

/**
 * 确认设计方案
 * @param {Number} designId - 设计ID
 */
export const confirmDesign = (designId) => {
  return post(`/api/client/design/confirm/${designId}`)
}

// ==================== 预算相关 ====================

/**
 * 获取预算汇总
 */
export const getBudgetSummary = () => {
  return get('/api/client/budget/summary')
}

/**
 * 获取预算明细
 * @param {String} category - 分类
 */
export const getBudgetDetail = (category) => {
  return get('/api/client/budget/detail', { category })
}

// ==================== 物料清单 ====================

/**
 * 获取物料列表
 * @param {Object} params - { category, status, page, pageSize }
 */
export const getMaterialList = (params) => {
  return get('/api/client/material/list', params)
}

/**
 * 更新物料选择状态
 * @param {Number} materialId - 物料ID
 * @param {Boolean} selected - 是否选中
 */
export const updateMaterialSelect = (materialId, selected) => {
  return post(`/api/client/material/select/${materialId}`, { selected })
}

// ==================== 项目日志 ====================

/**
 * 获取日志列表
 * @param {Object} params - { page, pageSize }
 */
export const getLogList = (params) => {
  return get('/api/client/log/list', params)
}

/**
 * 获取日志详情
 * @param {Number} logId - 日志ID
 */
export const getLogDetail = (logId) => {
  return get(`/api/client/log/detail/${logId}`)
}

// ==================== 品牌相关 ====================

/**
 * 获取品牌信息
 */
export const getBrandInfo = () => {
  return get('/api/client/brand/info')
}

/**
 * 获取团队成员
 */
export const getTeamMembers = () => {
  return get('/api/client/brand/team')
}

