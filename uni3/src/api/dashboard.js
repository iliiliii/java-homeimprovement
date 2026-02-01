/**
 * 首页数据API
 */
import { get } from '@/utils/request'

/**
 * 获取客户首页数据
 */
export const getCustomerDashboard = (options = {}) => {
  return get('/app/dashboard/customer', {}, options)
}

/**
 * 获取员工首页数据
 * @param {Number} pageNum - 页码（可选，默认1）
 */
export const getStaffDashboard = (pageNum = 1, options = {}) => {
  return get('/app/dashboard/staff', { pageNum }, options)
}

/**
 * 获取项目详情
 * @param {String} projectId - 项目ID
 */
export const getProjectDetail = (projectId) => {
  return get(`/app/dashboard/project/${projectId}`)
}

/**
 * 获取项目进度列表
 * @param {String} projectId - 项目ID
 */
export const getProjectSchedules = (projectId) => {
  return get(`/app/dashboard/project/${projectId}/schedules`)
}

/**
 * 获取项目质检记录
 * @param {String} projectId - 项目ID
 */
export const getProjectInspections = (projectId) => {
  return get(`/app/dashboard/project/${projectId}/inspections`)
}

/**
 * 获取项目设计图列表
 * @param {String} projectId - 项目ID
 */
export const getProjectDesigns = (projectId) => {
  return get(`/app/dashboard/project/${projectId}/designs`)
}

/**
 * 获取项目房间列表（设计稿）
 * @param {String} projectId - 项目ID
 */
export const getProjectRooms = (projectId) => {
  return get(`/app/dashboard/project/${projectId}/rooms`)
}

/**
 * 获取项目合同金额列表
 * @param {String} projectId - 项目ID
 * @returns {Promise} 返回六个固定分类的合同金额
 */
export const getProjectContractAmounts = (projectId, options = {}) => {
  return get(`/app/dashboard/project/${projectId}/contracts`, {}, options)
}
