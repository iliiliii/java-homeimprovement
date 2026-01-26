import request from '@/utils/request'

// 查询微信绑定列表
export function listWechatBindings(query) {
  return request({
    url: '/evs/appWechatBindings/list',
    method: 'get',
    params: query
  })
}

// 查询微信绑定详细
export function getWechatBinding(id) {
  return request({
    url: '/evs/appWechatBindings/' + id,
    method: 'get'
  })
}

// 管理员解除微信绑定（通过openId）
export function unbindWechatByOpenId(openId) {
  return request({
    url: '/evs/appWechatBindings/unbind/openid/' + openId,
    method: 'delete'
  })
}

// 管理员解除微信绑定（通过userId）
export function unbindWechatByUserId(userId) {
  return request({
    url: '/evs/appWechatBindings/unbind/user/' + userId,
    method: 'delete'
  })
}

// 小程序端解除微信绑定（需要验证）
export function unbindWechat(data) {
  return request({
    url: '/app/auth/unbind-wechat',
    method: 'post',
    data: data
  })
}
