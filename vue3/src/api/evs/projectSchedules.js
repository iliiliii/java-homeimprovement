import request from '@/utils/request'

// 查询项目进度列表
export function listProjectSchedules(query) {
  return request({
    url: '/evs/projectSchedules/list',
    method: 'get',
    params: query
  })
}

// 查询项目进度详细
export function getProjectSchedules(id) {
  return request({
    url: '/evs/projectSchedules/' + id,
    method: 'get'
  })
}

// 新增项目进度
export function addProjectSchedules(data) {
  return request({
    url: '/evs/projectSchedules',
    method: 'post',
    data: data
  })
}

// 修改项目进度
export function updateProjectSchedules(data) {
  return request({
    url: '/evs/projectSchedules',
    method: 'put',
    data: data
  })
}

// 删除项目进度
export function delProjectSchedules(id) {
  return request({
    url: '/evs/projectSchedules/' + id,
    method: 'delete'
  })
}

// 更新项目进度排序
export function updateProjectSchedulesOrder(id, stageOrder) {
  // 确保 stageOrder 是数字类型，且不为 null/undefined
  const orderValue = stageOrder != null ? Number(stageOrder) : null
  if (orderValue === null || isNaN(orderValue)) {
    console.error('无效的 stageOrder 值:', stageOrder)
    return Promise.reject(new Error('无效的排序值'))
  }
  console.log('更新排序API调用:', { id, stageOrder: orderValue })
  return request({
    url: '/evs/projectSchedules',
    method: 'put',
    data: { 
      id, 
      stageOrder: orderValue 
    }
  })
}
