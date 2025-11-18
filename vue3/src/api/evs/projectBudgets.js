import request from '@/utils/request'

// 查询项目预算列表
export function listProjectBudgets(query) {
  return request({
    url: '/evs/projectBudgets/list',
    method: 'get',
    params: query
  })
}

// 查询项目预算详细
export function getProjectBudgets(id) {
  return request({
    url: '/evs/projectBudgets/' + id,
    method: 'get'
  })
}

// 新增项目预算
export function addProjectBudgets(data) {
  return request({
    url: '/evs/projectBudgets',
    method: 'post',
    data: data
  })
}

// 修改项目预算
export function updateProjectBudgets(data) {
  return request({
    url: '/evs/projectBudgets',
    method: 'put',
    data: data
  })
}

// 删除项目预算
export function delProjectBudgets(id) {
  return request({
    url: '/evs/projectBudgets/' + id,
    method: 'delete'
  })
}
