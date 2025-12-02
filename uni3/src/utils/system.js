/**
 * 系统信息工具类
 * System Information Utilities
 */

let systemInfo = null
let menuButtonInfo = null
let navBarHeight = 0
let statusBarHeight = 0

/**
 * 获取系统信息
 */
export const getSystemInfo = () => {
    if (systemInfo) return systemInfo

    try {
        systemInfo = uni.getSystemInfoSync()
        statusBarHeight = systemInfo.statusBarHeight || 0

        // #ifdef MP-WEIXIN
        // 获取胶囊按钮位置信息
        try {
            menuButtonInfo = uni.getMenuButtonBoundingClientRect()

            // 计算导航栏高度
            // 胶囊按钮高度 + 胶囊按钮上下间距 * 2
            const menuButtonHeight = menuButtonInfo.height
            const menuButtonTop = menuButtonInfo.top

            navBarHeight = menuButtonHeight + (menuButtonTop - statusBarHeight) * 2
        } catch (e) {
            // 如果获取失败，使用默认值
            navBarHeight = 44
        }
        // #endif

        // #ifndef MP-WEIXIN
        // H5 和其他平台使用固定高度
        navBarHeight = 44
        // #endif
    } catch (e) {
        console.error('获取系统信息失败:', e)
        systemInfo = {
            statusBarHeight: 20,
            windowWidth: 375,
            windowHeight: 667
        }
        statusBarHeight = 20
        navBarHeight = 44
    }

    return systemInfo
}

/**
 * 获取状态栏高度
 */
export const getStatusBarHeight = () => {
    if (statusBarHeight === 0) {
        getSystemInfo()
    }
    return statusBarHeight
}

/**
 * 获取自定义导航栏高度
 */
export const getNavBarHeight = () => {
    if (navBarHeight === 0) {
        getSystemInfo()
    }
    return navBarHeight
}

/**
 * 获取胶囊按钮信息（仅微信小程序）
 */
export const getMenuButtonInfo = () => {
    if (!menuButtonInfo) {
        getSystemInfo()
    }
    return menuButtonInfo
}

/**
 * 获取导航栏总高度（状态栏 + 导航栏）
 */
export const getTotalNavHeight = () => {
    return getStatusBarHeight() + getNavBarHeight()
}

/**
 * 将 rpx 转换为 px
 */
export const rpxToPx = (rpx) => {
    const systemInfo = getSystemInfo()
    const screenWidth = systemInfo.windowWidth || 375
    return (rpx / 750) * screenWidth
}

/**
 * 将 px 转换为 rpx
 */
export const pxToRpx = (px) => {
    const systemInfo = getSystemInfo()
    const screenWidth = systemInfo.windowWidth || 375
    return (px / screenWidth) * 750
}

export default {
    getSystemInfo,
    getStatusBarHeight,
    getNavBarHeight,
    getMenuButtonInfo,
    getTotalNavHeight,
    rpxToPx,
    pxToRpx
}
