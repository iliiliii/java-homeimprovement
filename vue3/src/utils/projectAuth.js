import { computed } from 'vue'
import useUserStore from '@/store/modules/user'
import { listProjectMembers } from '@/api/evs/projectMembers'

/**
 * 项目权限控制工具
 * 提供基于角色的项目访问权限控制
 */
export const useProjectAuth = () => {
  const userStore = useUserStore()

  /**
   * 判断当前用户是否为管理员
   * @returns {ComputedRef<boolean>}
   */
  const isAdmin = computed(() => {
    const roles = userStore.roles || []
    return roles.includes('admin') || roles.includes('admin')
  })

  /**
   * 获取当前用户关联的项目ID列表
   * @returns {Promise<string[]|null>} 管理员返回null，非管理员返回项目ID数组
   */
  const getUserProjectIds = async () => {
    // 如果是管理员，不限制项目访问
    if (isAdmin.value) {
      return null
    }

    try {
      const userId = userStore.id
      if (!userId) {
        console.warn('用户ID未找到，权限检查失败')
        return []
      }

      const response = await listProjectMembers({
        userId: userId.toString(),
        isActive: 1
      })

      if (!response.rows || response.rows.length === 0) {
        console.info('用户没有关联任何项目')
        return []
      }

      // 提取项目ID列表并去重
      const projectIds = response.rows
        .map(member => member.projectId)
        .filter(projectId => projectId && projectId.trim() !== '')
        .filter((value, index, array) => array.indexOf(value) === index) // 去重

      return projectIds
    } catch (error) {
      console.error('获取用户项目关联失败:', error)
      return []
    }
  }

  /**
   * 获取用户的项目角色信息
   * @returns {Promise<Array>} 项目成员信息数组
   */
  const getUserProjectRoles = async () => {
    try {
      const userId = userStore.id
      if (!userId) {
        return []
      }

      const response = await listProjectMembers({
        userId: userId.toString(),
        isActive: 1
      })

      return response.rows || []
    } catch (error) {
      console.error('获取用户项目角色失败:', error)
      return []
    }
  }

  /**
   * 判断用户是否可以访问指定项目
   * @param {string} projectId 项目ID
   * @returns {Promise<boolean>} 是否有访问权限
   */
  const canAccessProject = async (projectId) => {
    // 管理员可以访问所有项目
    if (isAdmin.value) {
      return true
    }

    // 非管理员检查是否在该项目中
    const projectIds = await getUserProjectIds()
    return projectIds && projectIds.includes(projectId)
  }

  /**
   * 获取用户在项目中的最高角色
   * @returns {Promise<string>} 角色标识
   */
  const getUserHighestRole = async () => {
    const userRoles = await getUserProjectRoles()
    if (userRoles.length === 0) {
      return 'none'
    }

    // 角色优先级: PM > DESIGNER > SUPERVISOR > WORKER
    const rolePriority = {
      'PM': 4,
      'DESIGNER': 3,
      'SUPERVISOR': 2,
      'WORKER': 1
    }

    let highestRole = 'WORKER'
    let highestPriority = 0

    userRoles.forEach(member => {
      const priority = rolePriority[member.role] || 0
      if (priority > highestPriority) {
        highestPriority = priority
        highestRole = member.role
      }
    })

    return highestRole
  }

  return {
    isAdmin,
    getUserProjectIds,
    getUserProjectRoles,
    canAccessProject,
    getUserHighestRole
  }
}