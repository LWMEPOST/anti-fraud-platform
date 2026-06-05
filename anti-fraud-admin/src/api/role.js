import request from '@/utils/request'

export function getRoleList(params) {
  return request({
    url: '/role/list',
    method: 'get',
    params
  })
}

export function getAllRoles() {
  return request({
    url: '/role/all',
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

export function updateRole(id, data) {
  return request({
    url: `/role/${id}`,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

export function getPermissionTree() {
  return request({
    url: '/role/permission/tree',
    method: 'get'
  })
}

export function getRolePermissions(id) {
  return request({
    url: `/role/${id}/permissions`,
    method: 'get'
  })
}

export function assignPermissions(id, permissionIds) {
  return request({
    url: `/role/${id}/permissions`,
    method: 'put',
    data: permissionIds
  })
}
