import request from '@/utils/request'

// 查询项目客户列表
export function listProjectCustomers(query) {
  return request({
    url: '/evs/projectCustomers/list',
    method: 'get',
    params: query
  })
}

// 查询项目客户详细
export function getProjectCustomers(id) {
  return request({
    url: '/evs/projectCustomers/' + id,
    method: 'get'
  })
}

// 新增项目客户
export function addProjectCustomers(data) {
  return request({
    url: '/evs/projectCustomers',
    method: 'post',
    data: data
  })
}

// 修改项目客户
export function updateProjectCustomers(data) {
  return request({
    url: '/evs/projectCustomers',
    method: 'put',
    data: data
  })
}

// 删除项目客户
export function delProjectCustomers(id) {
  return request({
    url: '/evs/projectCustomers/' + id,
    method: 'delete'
  })
}

// 查询项目的所有客户
export function getProjectCustomersList(projectId) {
  return request({
    url: `/evs/projectCustomers/project/${projectId}`,
    method: 'get'
  })
}

// 查询客户的所有项目
export function getCustomerProjectsList(customerId) {
  return request({
    url: `/evs/projectCustomers/customer/${customerId}`,
    method: 'get'
  })
}

// 查询项目的主客户
export function getPrimaryCustomer(projectId) {
  return request({
    url: `/evs/projectCustomers/primary/${projectId}`,
    method: 'get'
  })
}

// 添加客户到项目
export function addCustomersToProject(data) {
  return request({
    url: '/evs/projectCustomers/addCustomers',
    method: 'post',
    data: data
  })
}

// 从项目移除客户
export function removeCustomerFromProject(data) {
  return request({
    url: '/evs/projectCustomers/removeCustomer',
    method: 'post',
    data: data
  })
}

// 设置主客户
export function setPrimaryCustomer(data) {
  return request({
    url: '/evs/projectCustomers/setPrimary',
    method: 'post',
    data: data
  })
}

// 检查客户访问权限
export function checkCustomerAccess(projectId, customerId) {
  return request({
    url: '/evs/projectCustomers/checkAccess',
    method: 'get',
    params: {
      projectId,
      customerId
    }
  })
}
