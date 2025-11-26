import request from '@/utils/request'

// 查询质量检测列表
export function listQualityInspections(query) {
  return request({
    url: '/evs/qualityInspections/list',
    method: 'get',
    params: query
  })
}

// 查询质量检测详细
export function getQualityInspections(id) {
  return request({
    url: '/evs/qualityInspections/' + id,
    method: 'get'
  })
}

// 新增质量检测
export function addQualityInspections(data) {
  return request({
    url: '/evs/qualityInspections',
    method: 'post',
    data: data
  })
}

// 修改质量检测
export function updateQualityInspections(data) {
  return request({
    url: '/evs/qualityInspections',
    method: 'put',
    data: data
  })
}

// 删除质量检测
export function delQualityInspections(id) {
  return request({
    url: '/evs/qualityInspections/' + id,
    method: 'delete'
  })
}
