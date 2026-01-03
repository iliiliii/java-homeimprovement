import request from '@/utils/request'

/**
 * 获取所有用户-岗位关联
 */
export function getAllUserPost() {
  return request({
    url: '/evs/userPost/all',
    method: 'get'
  })
}

/**
 * 根据岗位ID获取用户列表
 */
export function getUsersByPostId(postId) {
  return request({
    url: '/evs/userPost/users/' + postId,
    method: 'get'
  })
}
