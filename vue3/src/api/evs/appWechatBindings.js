import request from '@/utils/request'

// 查询微信绑定列表
export function listAppWechatBindings(query) {
  return request({
    url: '/evs/appWechatBindings/list',
    method: 'get',
    params: query
  })
}

// 查询微信绑定详细
export function getAppWechatBindings(id) {
  return request({
    url: '/evs/appWechatBindings/' + id,
    method: 'get'
  })
}

// 新增微信绑定
export function addAppWechatBindings(data) {
  return request({
    url: '/evs/appWechatBindings',
    method: 'post',
    data: data
  })
}

// 修改微信绑定
export function updateAppWechatBindings(data) {
  return request({
    url: '/evs/appWechatBindings',
    method: 'put',
    data: data
  })
}

// 删除微信绑定
export function delAppWechatBindings(id) {
  return request({
    url: '/evs/appWechatBindings/' + id,
    method: 'delete'
  })
}
