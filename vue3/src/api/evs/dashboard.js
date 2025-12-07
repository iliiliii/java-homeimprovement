import request from '@/utils/request'

// 获取首页统计数据（管理员视角）
export function getDashboardStats() {
  return request({
    url: '/evs/dashboard/stats',
    method: 'get'
  })
}

// 获取重点客户列表（带项目统计）
export function getTopCustomers(limit = 5) {
  return request({
    url: '/evs/dashboard/topCustomers',
    method: 'get',
    params: { limit }
  })
}

// 获取进行中的项目列表
export function getInProgressProjects(limit = 5) {
  return request({
    url: '/evs/dashboard/inProgressProjects',
    method: 'get',
    params: { limit }
  })
}

// 获取待办事项统计
export function getTodoStats() {
  return request({
    url: '/evs/dashboard/todoStats',
    method: 'get'
  })
}
