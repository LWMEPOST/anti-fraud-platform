import request from '@/utils/request'

export function getPublishedContent(params) {
  return request({
    url: '/content/published',
    method: 'get',
    params
  })
}

export function getLatestContent(limit = 6) {
  return request({
    url: '/content/latest',
    method: 'get',
    params: { limit }
  })
}

export function getContentList(params) {
  return request({
    url: '/content/list',
    method: 'get',
    params
  })
}

export function getContentById(id) {
  return request({
    url: `/content/${id}`,
    method: 'get'
  })
}

export function addContent(data) {
  return request({
    url: '/content',
    method: 'post',
    data
  })
}

export function updateContent(id, data) {
  return request({
    url: `/content/${id}`,
    method: 'put',
    data
  })
}

export function deleteContent(id) {
  return request({
    url: `/content/${id}`,
    method: 'delete'
  })
}

export function auditContent(id, status) {
  return request({
    url: `/content/${id}/audit`,
    method: 'put',
    params: { status }
  })
}

export function likeContent(id) {
  return request({
    url: `/content/${id}/like`,
    method: 'post'
  })
}

export function uploadContentMedia(formData, type) {
  return request({
    url: '/media/upload',
    method: 'post',
    params: { type },
    data: formData
  })
}

export function getUserMenus(userId) {
  return request({
    url: `/menu/user/${userId}`,
    method: 'get'
  })
}
