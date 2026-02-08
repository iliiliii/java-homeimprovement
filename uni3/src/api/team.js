/**
 * 团队成员API
 */
import { get } from '@/utils/request'

/**
 * 获取团队成员列表
 * 过滤条件：email不为空、未删除、未停用
 */
export const getTeamMembers = (options = {}) => {
  return get('/app/team/members', {}, options)
}
