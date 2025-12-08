import request from '@/utils/request'

// 查询新闻咨询设置列表
export function listNewsConsultation(query) {
  return request({
    url: '/evs/newsConsultation/list',
    method: 'get',
    params: query
  })
}

// 查询新闻咨询设置详细
export function getNewsConsultation(id) {
  return request({
    url: '/evs/newsConsultation/' + id,
    method: 'get'
  })
}

// 新增新闻咨询设置
export function addNewsConsultation(data) {
  return request({
    url: '/evs/newsConsultation',
    method: 'post',
    data: data
  })
}

// 修改新闻咨询设置
export function updateNewsConsultation(data) {
  return request({
    url: '/evs/newsConsultation',
    method: 'put',
    data: data
  })
}

// 删除新闻咨询设置
export function delNewsConsultation(id) {
  return request({
    url: '/evs/newsConsultation/' + id,
    method: 'delete'
  })
}
