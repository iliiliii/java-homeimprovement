/**
 * 质量问题API
 */
import { get, post } from '@/utils/request'

/**
 * 获取项目质量问题列表
 * @param {Object} params - 查询参数
 * @param {Number} params.page - 页码
 * @param {Number} params.pageSize - 每页数量
 */
export const getQualityIssueList = (params = {}) => {
  return get('/app/qualityIssues/list', params)
}

/**
 * 问题上报
 * @param {Object} data - 问题数据
 * @param {String} data.title - 问题标题
 * @param {String} data.description - 问题描述
 * @param {String} data.inspectionType - 质检类型（施工阶段）
 * @param {String} data.category - 问题分类
 * @param {String} data.location - 问题位置
 * @param {String} data.dueDate - 整改期限
 * @param {String} data.images - 现场照片（JSON数组）
 * @param {String} data.inspectionDate - 检查日期
 */
export const reportQualityIssue = (data) => {
  return post('/app/qualityIssues/report', data)
}

/**
 * 获取质量问题详情
 * @param {String} issueId - 问题ID
 */
export const getQualityIssueDetail = (issueId) => {
  return get(`/app/qualityIssues/${issueId}`)
}

/**
 * 获取字典数据
 * @param {String} dictType - 字典类型
 * @returns {Promise<Array>} 字典数据列表 [{value, label}]
 */
export const getDictData = (dictType) => {
  return get(`/app/dict/${dictType}`)
}

/**
 * 获取问题的整改记录列表
 * @param {String} issueId - 问题ID
 * @returns {Promise<Array>} 整改记录列表
 */
export const getFixesByIssueId = (issueId) => {
  return get(`/app/qualityFixes/byIssue/${issueId}`)
}

/**
 * 提交整改记录
 * @param {Object} data - 整改数据
 * @param {String} data.issueId - 问题ID
 * @param {String} data.fixDescription - 整改描述
 * @param {String} data.images - 整改图片（JSON数组）
 * @param {String} data.status - 整改状态（OPEN/IN_PROGRESS/RESOLVED）
 */
export const submitFix = (data) => {
  return post('/app/qualityFixes/submit', data)
}
