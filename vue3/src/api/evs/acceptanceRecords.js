import request from '@/utils/request'

// 查询验收记录列表
export function listAcceptanceRecords(query) {
  return request({
    url: '/evs/acceptanceRecords/list',
    method: 'get',
    params: query
  })
}

// 查询验收记录详细
export function getAcceptanceRecords(id) {
  return request({
    url: '/evs/acceptanceRecords/' + id,
    method: 'get'
  })
}

// 根据进度ID获取验收记录列表
export function getAcceptanceRecordsByScheduleId(scheduleId) {
  return request({
    url: '/evs/acceptanceRecords/bySchedule/' + scheduleId,
    method: 'get'
  })
}

// 新增验收记录
export function addAcceptanceRecords(data) {
  return request({
    url: '/evs/acceptanceRecords',
    method: 'post',
    data: data
  })
}

// 修改验收记录
export function updateAcceptanceRecords(data) {
  return request({
    url: '/evs/acceptanceRecords',
    method: 'put',
    data: data
  })
}

// 删除验收记录
export function delAcceptanceRecords(id) {
  return request({
    url: '/evs/acceptanceRecords/' + id,
    method: 'delete'
  })
}