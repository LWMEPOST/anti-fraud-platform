import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function batchDeleteUsers(ids) {
  return request({
    url: '/user/batch',
    method: 'delete',
    data: ids
  })
}

export function resetPassword(id) {
  return request({
    url: `/user/${id}/resetPwd`,
    method: 'post'
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getUserRoles(id) {
  return request({
    url: `/user/${id}/roles`,
    method: 'get'
  })
}

export function assignRoles(id, roleIds) {
  return request({
    url: `/user/${id}/roles`,
    method: 'put',
    data: roleIds
  })
}
