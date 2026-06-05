import request from '@/utils/request'

export function getCurrentUserMenus() {
  return request({
    url: '/menu/current',
    method: 'get'
  })
}
