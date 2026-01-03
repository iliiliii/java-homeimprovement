/**
 * 项目进度相关API
 */
import { get, post, put, del } from '@/utils/request'
// import { 
//   getMockProjectScheduleList, 
//   getMockProjectScheduleRecordList, 
//   getMockProjectScheduleRecordDetail 
// } from './mockProjectSchedule'

// 安全的环境检查
const getEnvironment = () => {
  try {
    return process?.env?.NODE_ENV || 'development'
  } catch (e) {
    return 'development'
  }
}

// 判断是否使用模拟数据 - 修改为始终使用真实API
const USE_MOCK = false // 开发环境也使用真实API

/**
 * 获取项目进度列表
 * @returns {Promise} 进度列表
 */
export const getProjectScheduleList = async () => {
  try {
    if (USE_MOCK) {
      // return await getMockProjectScheduleList()
    }
    
    // 调用真实API
    const data = await get('/app/projectSchedules/list')
    return data
  } catch (error) {
    console.error('API调用失败，使用模拟数据作为兜底:', error)
    
    // API调用失败时，使用模拟数据作为兜底
    try {
      // return await getMockProjectScheduleList()
    } catch (mockError) {
      console.error('模拟数据也加载失败:', mockError)
      throw new Error('数据加载失败')
    }
  }
}

/**
 * 获取项目进度验收记录列表
 * @param {Object} params - 查询参数
 * @param {String} params.scheduleId - 进度ID（可选）
 * @param {Number} params.page - 页码
 * @param {Number} params.pageSize - 页大小
 * @returns {Promise} 验收记录列表
 */
export const getProjectScheduleRecordList = async (params = {}) => {
  try {
    if (USE_MOCK) {
      return await getMockProjectScheduleRecordList(params)
    }
    
    const data = await get('/app/projectScheduleRecords/list', params)
    return data
  } catch (error) {
    console.error('获取验收记录列表失败，使用模拟数据:', error)
    return await getMockProjectScheduleRecordList(params)
  }
}

/**
 * 获取进度验收记录详情
 * @param {String} recordId - 记录ID
 * @returns {Promise} 记录详情
 */
export const getProjectScheduleRecordDetail = async (recordId) => {
  try {
    if (USE_MOCK) {
      // return await getMockProjectScheduleRecordDetail(recordId)
    }
    
    const data = await get(`/app/projectScheduleRecords/${recordId}`)
    return data
  } catch (error) {
    console.error('获取验收记录详情失败，使用模拟数据:', error)
    // return await getMockProjectScheduleRecordDetail(recordId)
  }
}

/**
 * 新增验收记录
 * @param {Object} data - 验收记录数据
 * @returns {Promise} 新增结果
 */
export const addAcceptanceRecord = async (data) => {
  return await post('/app/projectScheduleRecords', data)
}

/**
 * 编辑验收记录
 * @param {String} recordId - 记录ID
 * @param {Object} data - 验收记录数据
 * @returns {Promise} 编辑结果
 */
export const updateAcceptanceRecord = async (recordId, data) => {
  return await put(`/app/projectScheduleRecords/${recordId}`, data)
}

/**
 * 删除验收记录
 * @param {String} recordId - 记录ID
 * @returns {Promise} 删除结果
 */
export const deleteAcceptanceRecord = async (recordId) => {
  return await del(`/app/projectScheduleRecords/${recordId}`)
}