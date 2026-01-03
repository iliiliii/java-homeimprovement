import request from '@/utils/request'

// 查询项目房间列表
export function listProjectRooms(query) {
  return request({
    url: '/evs/projectRooms/list',
    method: 'get',
    params: query
  })
}

// 查询项目房间详细
export function getProjectRooms(id) {
  return request({
    url: '/evs/projectRooms/' + id,
    method: 'get'
  })
}

// 新增项目房间
export function addProjectRooms(data) {
  return request({
    url: '/evs/projectRooms',
    method: 'post',
    data: data
  })
}

// 修改项目房间
export function updateProjectRooms(data) {
  return request({
    url: '/evs/projectRooms',
    method: 'put',
    data: data
  })
}

// 删除项目房间
export function delProjectRooms(id) {
  return request({
    url: '/evs/projectRooms/' + id,
    method: 'delete'
  })
}

// 更新项目房间排序
export function updateProjectRoomsOrder(id, sortOrder) {
  return request({
    url: '/evs/projectRooms',
    method: 'put',
    data: { id, sortOrder }
  })
}
