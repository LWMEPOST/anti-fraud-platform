import { defineStore } from 'pinia'
import { login as loginApi, getUserById } from '@/api/user'
import { getCurrentUserMenus } from '@/api/menu'

const readStoredJson = (key, fallback) => {
  try {
    const rawValue = localStorage.getItem(key)
    return rawValue ? JSON.parse(rawValue) : fallback
  } catch (error) {
    return fallback
  }
}

const normalizeRoleCodes = (roleCodes) => Array.isArray(roleCodes) ? roleCodes : []

const mergeUserInfoWithRoles = (userInfo, roleCodes) => ({
  ...(userInfo || {}),
  roleCodes: normalizeRoleCodes(roleCodes)
})

const flattenPermissionCodes = (menus = []) => {
  const permissionSet = new Set()

  const walk = (items = []) => {
    items.forEach((item) => {
      if (item?.permissionCode) {
        permissionSet.add(item.permissionCode)
      }

      if (Array.isArray(item?.children) && item.children.length > 0) {
        walk(item.children)
      }
    })
  }

  walk(menus)
  return [...permissionSet]
}

const persistSession = (token, userInfo, roleCodes, permissions) => {
  localStorage.setItem('token', token || '')
  localStorage.setItem('userInfo', JSON.stringify(userInfo || {}))
  localStorage.setItem('roleCodes', JSON.stringify(normalizeRoleCodes(roleCodes)))
  localStorage.setItem('permissions', JSON.stringify(Array.isArray(permissions) ? permissions : []))
}

const clearSession = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('roleCodes')
  localStorage.removeItem('permissions')
}

const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: readStoredJson('userInfo', {}),
    roleCodes: readStoredJson('roleCodes', []),
    permissions: readStoredJson('permissions', []),
    permissionsLoaded: false
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || '',
    isAdmin: (state) => state.roleCodes.includes('ADMIN'),
    isContentAdmin: (state) => state.roleCodes.includes('CONTENT_ADMIN'),

    canAccess: (state) => (roles = []) => {
      if (!roles.length) return true
      return roles.some(role => state.roleCodes.includes(role))
    },

    hasPermission: (state) => (permissionCode) => {
      if (!permissionCode) return true
      return state.permissions.includes(permissionCode)
    },

    hasAnyPermission: (state) => (permissionCodes = []) => {
      if (!permissionCodes.length) return true
      return permissionCodes.some(permissionCode => state.permissions.includes(permissionCode))
    }
  },

  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      const roleCodes = normalizeRoleCodes(res.data.roleCodes)

      this.token = res.data.token
      this.roleCodes = roleCodes
      this.userInfo = mergeUserInfoWithRoles(res.data.user, roleCodes)
      this.permissions = []
      this.permissionsLoaded = false

      persistSession(this.token, this.userInfo, this.roleCodes, this.permissions)
      await this.fetchPermissions()
      return res
    },

    async fetchPermissions() {
      if (!this.token) {
        this.permissions = []
        this.permissionsLoaded = false
        persistSession(this.token, this.userInfo, this.roleCodes, this.permissions)
        return []
      }

      const res = await getCurrentUserMenus()
      this.permissions = flattenPermissionCodes(res.data || [])
      this.permissionsLoaded = true

      persistSession(this.token, this.userInfo, this.roleCodes, this.permissions)
      return this.permissions
    },

    logout() {
      this.token = ''
      this.userInfo = {}
      this.roleCodes = []
      this.permissions = []
      this.permissionsLoaded = false

      clearSession()

      if (window.location.pathname !== '/login') {
        window.location.replace('/login')
      }
    },

    async fetchUserInfo() {
      if (!this.userInfo?.id) {
        if (!this.permissionsLoaded && this.token) {
          await this.fetchPermissions()
        }
        return
      }

      const res = await getUserById(this.userInfo.id)
      const roleCodes = normalizeRoleCodes(
        res.data.roleCodes?.length ? res.data.roleCodes : this.roleCodes
      )

      this.roleCodes = roleCodes
      this.userInfo = mergeUserInfoWithRoles(res.data, roleCodes)

      persistSession(this.token, this.userInfo, this.roleCodes, this.permissions)

      if (!this.permissionsLoaded && this.token) {
        await this.fetchPermissions()
      }
    }
  }
})

export default useUserStore
