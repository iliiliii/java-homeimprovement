import request from '@/utils/request'

// 查询文件上传列表
export function listFileUploads(query) {
  return request({
    url: '/evs/fileUploads/list',
    method: 'get',
    params: query
  })
}

// 查询文件上传详细
export function getFileUploads(id) {
  return request({
    url: '/evs/fileUploads/' + id,
    method: 'get'
  })
}

// 新增文件上传
export function addFileUploads(data) {
  return request({
    url: '/evs/fileUploads',
    method: 'post',
    data: data
  })
}

// 修改文件上传
export function updateFileUploads(data) {
  return request({
    url: '/evs/fileUploads',
    method: 'put',
    data: data
  })
}

// 删除文件上传
export function delFileUploads(id) {
  return request({
    url: '/evs/fileUploads/' + id,
    method: 'delete'
  })
}
