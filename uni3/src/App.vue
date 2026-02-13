<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { preloadGuestConfig } from '@/config/guest'

// 小程序更新管理器
let updateManager = null

/**
 * 初始化小程序更新检查
 * 完整的更新流程：检查更新 -> 下载更新 -> 应用更新
 */
const initUpdateManager = () => {
  // #ifdef MP-WEIXIN
  try {
    // 获取全局唯一的版本更新管理器
    updateManager = uni.getUpdateManager()
    
    if (!updateManager) {
      console.warn('[更新管理] 当前环境不支持更新管理器')
      return
    }
    
    console.log('[更新管理] 更新管理器初始化成功')
    
    // 1. 监听向微信后台请求检查更新结果事件
    updateManager.onCheckForUpdate((res) => {
      console.log('[更新管理] 检查更新结果:', res)
      
      if (res.hasUpdate) {
        console.log('[更新管理] 发现新版本，准备下载')
        
        // 提示用户发现新版本
        uni.showToast({
          title: '发现新版本',
          icon: 'none',
          duration: 2000
        })
      } else {
        console.log('[更新管理] 当前已是最新版本')
      }
    })
    
    // 2. 监听小程序有版本更新事件（新版本下载成功）
    updateManager.onUpdateReady(() => {
      console.log('[更新管理] 新版本下载完成，准备应用更新')
      
      // 弹窗提示用户新版本已经准备好，询问是否重启应用
      uni.showModal({
        title: '更新提示',
        content: '新版本已经准备好，是否重启应用？',
        confirmText: '立即重启',
        cancelText: '稍后',
        success: (res) => {
          if (res.confirm) {
            console.log('[更新管理] 用户确认重启，应用新版本')
            // 强制小程序重启并应用新版本
            updateManager.applyUpdate()
          } else {
            console.log('[更新管理] 用户选择稍后重启')
            
            // 提示用户下次启动时会自动更新
            uni.showToast({
              title: '下次启动时将自动更新',
              icon: 'none',
              duration: 2000
            })
          }
        }
      })
    })
    
    // 3. 监听小程序更新失败事件
    updateManager.onUpdateFailed((err) => {
      console.error('[更新管理] 新版本下载失败:', err)
      
      // 提示用户更新失败，建议删除小程序重新搜索打开
      uni.showModal({
        title: '更新失败',
        content: '新版本下载失败，建议您删除小程序后重新搜索打开',
        showCancel: false,
        confirmText: '我知道了'
      })
    })
    
    console.log('[更新管理] 更新监听器注册完成')
    
  } catch (error) {
    console.error('[更新管理] 初始化失败:', error)
  }
  // #endif
}

/**
 * 手动检查更新（可在需要时调用）
 */
const checkForUpdate = () => {
  // #ifdef MP-WEIXIN
  if (updateManager) {
    console.log('[更新管理] 手动触发更新检查')
    // 注意：checkForUpdate 是自动触发的，无需手动调用
    // 这里只是提供一个接口，实际上微信会在小程序冷启动时自动检查
  }
  // #endif
}

onLaunch(async () => {
  console.log('App Launch')
  
  // 初始化小程序更新检查
  initUpdateManager()
  
  // 预加载游客演示项目配置（无论是否登录都加载，提升后续页面加载速度）
  try {
    console.log('[App] 开始预加载游客演示项目配置')
    await preloadGuestConfig()
    console.log('[App] 游客演示项目配置预加载完成')
  } catch (error) {
    console.error('[App] 预加载游客配置失败:', error)
    // 预加载失败不影响应用启动
  }
})

onShow(() => {
  console.log('App Show')
  
  // 每次小程序从后台进入前台时，也可以检查更新
  // 注意：微信会自动检查，这里只是确保监听器已注册
  if (!updateManager) {
    initUpdateManager()
  }
})

onHide(() => {
  console.log('App Hide')
})

// 导出方法供其他页面使用（如果需要）
defineExpose({
  checkForUpdate
})
</script>

<style lang="scss">
// uview-plus 样式 (变量已通过 vite.config.js 全局引入)
@import 'uview-plus/index.scss';
// 全局样式
@import './styles/global.scss';
</style>

