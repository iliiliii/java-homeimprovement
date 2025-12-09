/**
 * 首页数据API
 */
import { get } from '@/utils/request'

/**
 * 获取客户首页数据
 */
export const getCustomerDashboard = () => {
  return get('/app/dashboard/customer')
}

/**
 * 获取员工首页数据
 */
export const getStaffDashboard = () => {
  return get('/app/dashboard/staff')
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
