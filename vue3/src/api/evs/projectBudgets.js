import request from '@/utils/request'

// 查询预算明细列表
export function listProjectBudgets(query) {
  return request({
    url: '/evs/projectBudgets/list',
    method: 'get',
    params: query
  })
}

// 查询预算明细详细
export function getProjectBudgets(id) {
  return request({
    url: '/evs/projectBudgets/' + id,
    method: 'get'
  })
}

// 新增预算明细
export function addProjectBudgets(data) {
  return request({
    url: '/evs/projectBudgets',
    method: 'post',
    data: data
  })
}

// 修改预算明细
export function updateProjectBudgets(data) {
  return request({
    url: '/evs/projectBudgets',
    method: 'put',
    data: data
  })
}

// 删除预算明细
export function delProjectBudgets(id) {
  return request({
    url: '/evs/projectBudgets/' + id,
    method: 'delete'
  })
}
