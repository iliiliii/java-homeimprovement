import request from '@/utils/request'

// 查询进度记录列表
export function listProjectScheduleRecords(query) {
  return request({
    url: '/evs/projectScheduleRecords/list',
    method: 'get',
    params: query
  })
}

// 查询进度记录详细
export function getProjectScheduleRecords(id) {
  return request({
    url: '/evs/projectScheduleRecords/' + id,
    method: 'get'
  })
}

// 新增进度记录
export function addProjectScheduleRecords(data) {
  return request({
    url: '/evs/projectScheduleRecords',
    method: 'post',
    data: data
  })
}

// 修改进度记录
export function updateProjectScheduleRecords(data) {
  return request({
    url: '/evs/projectScheduleRecords',
    method: 'put',
    data: data
  })
}

// 删除进度记录
export function delProjectScheduleRecords(id) {
  return request({
    url: '/evs/projectScheduleRecords/' + id,
    method: 'delete'
  })
}
