import request from '@/utils/request'

// 查询项目信息列表
export function listProjects(query) {
  return request({
    url: '/evs/projects/list',
    method: 'get',
    params: query
  })
}

// 查询项目信息列表（包含客户信息）
export function listProjectsWithCustomer(query) {
  return request({
    url: '/evs/projects/list',
    method: 'get',
    params: {
      ...query,
      includeCustomer: true
    }
  })
}

// 通过项目成员关联查询项目列表（支持管理员权限筛选）
export function listProjectsWithMembers(query) {
  return request({
    url: '/evs/projects/list',
    method: 'get',
    params: {
      ...query,
      includeProjectMembers: true
    }
  })
}

// 查询项目详细信息（包含客户信息）
export function getProjectWithCustomer(id) {
  return request({
    url: `/evs/projects/${id}`,
    method: 'get',
    params: {
      includeCustomer: true
    }
  })
}

// 查询项目信息详细
export function getProjects(id) {
  return request({
    url: '/evs/projects/' + id,
    method: 'get'
  })
}

// 新增项目信息
export function addProjects(data) {
  return request({
    url: '/evs/projects',
    method: 'post',
    data: data
  })
}

// 修改项目信息
export function updateProjects(data) {
  return request({
    url: '/evs/projects',
    method: 'put',
    data: data
  })
}

// 删除项目信息
export function delProjects(id) {
  return request({
    url: '/evs/projects/' + id,
    method: 'delete'
  })
}
