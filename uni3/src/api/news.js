/**
 * 资讯API
 */
import { get } from '@/utils/request'

/**
 * 获取Banner资讯列表（不分页）
 * @returns Promise<Array>
 */
export const getBannerNews = () => {
  return get('/app/news/list', { position: 'banner' })
}

/**
 * 获取资讯列表（分页）
 * @param {String} position - 发布位置：home/commercial
 * @param {Number} pageNum - 页码，默认1
 * @param {Number} pageSize - 每页数量，默认20
 * @returns Promise<Object> { list, total, pageNum, pageSize, hasMore }
 */
export const getNewsList = (position, pageNum = 1, pageSize = 20) => {
  return get('/app/news/list', { position, pageNum, pageSize })
}
