import request from '@/utils/request'

// 查询客户档案列表
export function listCustomers(query) {
  return request({
    url: '/evs/customers/list',
    method: 'get',
    params: query
  })
}

// 查询客户档案详细
export function getCustomers(id) {
  return request({
    url: '/evs/customers/' + id,
    method: 'get'
  })
}

// 新增客户档案
export function addCustomers(data) {
  return request({
    url: '/evs/customers',
    method: 'post',
    data: data
  })
}

// 修改客户档案
export function updateCustomers(data) {
  return request({
    url: '/evs/customers',
    method: 'put',
    data: data
  })
}

// 删除客户档案
export function delCustomers(id) {
  return request({
    url: '/evs/customers/' + id,
    method: 'delete'
  })
}
