/**
 * 小程序更新管理工具
 * 提供完整的小程序版本更新功能
 */

class UpdateManager {
  constructor() {
    this.manager = null
    this.isInitialized = false
  }

  /**
   * 初始化更新管理器
   * @param {Object} options 配置选项
   * @param {Boolean} options.silent 是否静默检查（不显示"当前已是最新版本"提示）
   * @param {Function} options.onCheckForUpdate 检查更新回调
   * @param {Function} options.onUpdateReady 更新准备就绪回调
   * @param {Function} options.onUpdateFailed 更新失败回调
   */
  init(options = {}) {
    // #ifdef MP-WEIXIN
    if (this.isInitialized) {
      console.log('[UpdateManager] 已经初始化，跳过')
      return
    }

    try {
      // 获取全局唯一的版本更新管理器
      this.manager = uni.getUpdateManager()

      if (!this.manager) {
        console.warn('[UpdateManager] 当前环境不支持更新管理器')
        return
      }

      console.log('[UpdateManager] 初始化成功')

      const {
        silent = true,
        onCheckForUpdate,
        onUpdateReady,
        onUpdateFailed
      } = options

      // 1. 监听检查更新结果
      this.manager.onCheckForUpdate((res) => {
        console.log('[UpdateManager] 检查更新结果:', res)

        if (res.hasUpdate) {
          console.log('[UpdateManager] 发现新版本')
          
          if (!silent) {
            uni.showToast({
              title: '发现新版本',
              icon: 'none',
              duration: 2000
            })
          }

          // 执行自定义回调
          if (typeof onCheckForUpdate === 'function') {
            onCheckForUpdate(res)
          }
        } else {
          console.log('[UpdateManager] 当前已是最新版本')
          
          if (!silent) {
            uni.showToast({
              title: '当前已是最新版本',
              icon: 'none',
              duration: 1500
            })
          }
        }
      })

      // 2. 监听新版本下载成功
      this.manager.onUpdateReady(() => {
        console.log('[UpdateManager] 新版本下载完成')

        // 执行自定义回调
        if (typeof onUpdateReady === 'function') {
          onUpdateReady()
        } else {
          // 默认行为：弹窗提示用户
          this.showUpdateDialog()
        }
      })

      // 3. 监听更新失败
      this.manager.onUpdateFailed((err) => {
        console.error('[UpdateManager] 更新失败:', err)

        // 执行自定义回调
        if (typeof onUpdateFailed === 'function') {
          onUpdateFailed(err)
        } else {
          // 默认行为：提示用户
          this.showUpdateFailedDialog()
        }
      })

      this.isInitialized = true
      console.log('[UpdateManager] 监听器注册完成')

    } catch (error) {
      console.error('[UpdateManager] 初始化失败:', error)
    }
    // #endif

    // #ifndef MP-WEIXIN
    console.log('[UpdateManager] 非微信小程序环境，跳过初始化')
    // #endif
  }

  /**
   * 显示更新对话框
   */
  showUpdateDialog() {
    uni.showModal({
      title: '更新提示',
      content: '新版本已经准备好，是否重启应用？',
      confirmText: '立即重启',
      cancelText: '稍后',
      success: (res) => {
        if (res.confirm) {
          console.log('[UpdateManager] 用户确认重启')
          this.applyUpdate()
        } else {
          console.log('[UpdateManager] 用户选择稍后重启')
          uni.showToast({
            title: '下次启动时将自动更新',
            icon: 'none',
            duration: 2000
          })
        }
      }
    })
  }

  /**
   * 显示更新失败对话框
   */
  showUpdateFailedDialog() {
    uni.showModal({
      title: '更新失败',
      content: '新版本下载失败，建议您删除小程序后重新搜索打开',
      showCancel: false,
      confirmText: '我知道了'
    })
  }

  /**
   * 应用更新（强制小程序重启）
   */
  applyUpdate() {
    // #ifdef MP-WEIXIN
    if (this.manager) {
      console.log('[UpdateManager] 应用更新，重启小程序')
      try {
        this.manager.applyUpdate()
      } catch (error) {
        console.error('[UpdateManager] 应用更新失败:', error)
        uni.showToast({
          title: '更新失败，请稍后重试',
          icon: 'none'
        })
      }
    }
    // #endif
  }

  /**
   * 手动检查更新
   * 注意：微信小程序会在冷启动时自动检查更新，此方法仅用于特殊场景
   */
  checkForUpdate() {
    // #ifdef MP-WEIXIN
    if (!this.isInitialized) {
      console.warn('[UpdateManager] 未初始化，先执行初始化')
      this.init({ silent: false })
    } else {
      console.log('[UpdateManager] 手动检查更新（微信会自动检查）')
      uni.showToast({
        title: '正在检查更新...',
        icon: 'loading',
        duration: 1500
      })
    }
    // #endif

    // #ifndef MP-WEIXIN
    uni.showToast({
      title: '当前环境不支持更新检查',
      icon: 'none'
    })
    // #endif
  }

  /**
   * 重置管理器（用于测试或特殊场景）
   */
  reset() {
    this.manager = null
    this.isInitialized = false
    console.log('[UpdateManager] 已重置')
  }
}

// 导出单例
export default new UpdateManager()
