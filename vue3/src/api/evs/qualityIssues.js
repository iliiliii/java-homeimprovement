import request from '@/utils/request'

// 查询质量问题列表
export function listQualityIssues(query) {
  return request({
    url: '/evs/qualityIssues/list',
    method: 'get',
    params: query
  })
}

// 查询质量问题详细
export function getQualityIssues(id) {
  return request({
    url: '/evs/qualityIssues/' + id,
    method: 'get'
  })
}

// 新增质量问题
export function addQualityIssues(data) {
  return request({
    url: '/evs/qualityIssues',
    method: 'post',
    data: data
  })
}

// 修改质量问题
export function updateQualityIssues(data) {
  return request({
    url: '/evs/qualityIssues',
    method: 'put',
    data: data
  })
}

// 删除质量问题
export function delQualityIssues(id) {
  return request({
    url: '/evs/qualityIssues/' + id,
    method: 'delete'
  })
}
