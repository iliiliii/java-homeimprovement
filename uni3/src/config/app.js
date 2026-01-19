/**
 * 应用配置文件
 * App Configuration
 */

export const APP_CONFIG = {
    // 应用名称
    name: '逅管家',

    // 应用副标题
    subtitle: '装修设计公司',

    // 应用全称
    fullName: '逅管家 · 装修设计公司',

    // 公司信息
    company: {
        name: '逅管家',
        description: '装修设计公司',
        slogan: '品质生活，从设计开始',

        // 联系方式
        contact: {
            phone: '400-XXX-XXXX',
            email: 'contact@houshidai.com',
            address: '中国 · 某某市某某区某某路123号'
        },

        // 社交媒体
        social: {
            wechat: 'houshidai_design',
            weibo: '@逅管家'
        }
    },

    // 版本信息
    version: {
        code: '1.0.0',
        name: 'v1.0.0',
        releaseDate: '2025-12-03'
    },

    // 版权信息
    copyright: {
        year: '2025',
        text: '逅管家 © 2025',
        icp: '' // 备案号（如需要）
    },

    // 功能开关
    features: {
        wechatLogin: true,
        rememberPassword: true,
        darkMode: false
    }
}

export default APP_CONFIG
