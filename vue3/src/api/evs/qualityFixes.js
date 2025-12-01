import request from '@/utils/request'

// 查询问题修复列表
export function listQualityFixes(query) {
  return request({
    url: '/evs/qualityFixes/list',
    method: 'get',
    params: query
  })
}

// 查询问题修复详细
export function getQualityFixes(id) {
  return request({
    url: '/evs/qualityFixes/' + id,
    method: 'get'
  })
}

// 新增问题修复
export function addQualityFixes(data) {
  return request({
    url: '/evs/qualityFixes',
    method: 'post',
    data: data
  })
}

// 修改问题修复
export function updateQualityFixes(data) {
  return request({
    url: '/evs/qualityFixes',
    method: 'put',
    data: data
  })
}

// 删除问题修复
export function delQualityFixes(id) {
  return request({
    url: '/evs/qualityFixes/' + id,
    method: 'delete'
  })
}
