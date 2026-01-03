import request from '@/utils/request'

// 查询项目成员列表
export function listProjectMembers(query) {
  return request({
    url: '/evs/projectMembers/list',
    method: 'get',
    params: query
  })
}

// 查询项目成员详细
export function getProjectMembers(id) {
  return request({
    url: '/evs/projectMembers/' + id,
    method: 'get'
  })
}

// 新增项目成员
export function addProjectMembers(data) {
  return request({
    url: '/evs/projectMembers',
    method: 'post',
    data: data
  })
}

// 修改项目成员
export function updateProjectMembers(data) {
  return request({
    url: '/evs/projectMembers',
    method: 'put',
    data: data
  })
}

// 删除项目成员
export function delProjectMembers(id) {
  return request({
    url: '/evs/projectMembers/' + id,
    method: 'delete'
  })
}

// 批量保存项目成员（一次性保存，替换原有成员）
export function batchSaveProjectMembers(data) {
  return request({
    url: '/evs/projectMembers/batchSave',
    method: 'post',
    data: data
  })
}
