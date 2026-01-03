import request from '@/utils/request'

// 查询项目附件信息列表
export function listProjectAttachment(query) {
  return request({
    url: '/evs/projectAttachment/list',
    method: 'get',
    params: query
  })
}

// 查询项目附件信息详细
export function getProjectAttachment(id) {
  return request({
    url: '/evs/projectAttachment/' + id,
    method: 'get'
  })
}

// 新增项目附件信息
export function addProjectAttachment(data) {
  return request({
    url: '/evs/projectAttachment',
    method: 'post',
    data: data
  })
}

// 修改项目附件信息
export function updateProjectAttachment(data) {
  return request({
    url: '/evs/projectAttachment',
    method: 'put',
    data: data
  })
}

// 删除项目附件信息
export function delProjectAttachment(id) {
  return request({
    url: '/evs/projectAttachment/' + id,
    method: 'delete'
  })
}

// 获取所有项目的合同总额
export function getTotalContractAmount() {
  return request({
    url: '/evs/projectAttachment/totalAmount',
    method: 'get'
  })
}
