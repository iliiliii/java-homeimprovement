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
