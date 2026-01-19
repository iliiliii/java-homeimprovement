/**
 * 联系方式配置
 */
export const CONTACT_CONFIG = {
  // 系统管理员联系方式
  admin: {
    name: '系统管理员',
    phone: '400-xxx-xxxx',
    wechat: 'admin_wechat',
    email: 'admin@company.com',
    workTime: '工作日 9:00-18:00'
  },
  
  // 技术支持联系方式
  support: {
    name: '技术支持',
    phone: '400-xxx-xxxx',
    wechat: 'support_wechat',
    email: 'support@company.com',
    workTime: '7x24小时'
  },
  
  // 常见问题
  faq: [
    {
      question: '为什么提示"手机号未注册"？',
      answer: '系统采用邀请制，需要管理员预先添加您的手机号才能登录。请联系管理员将您的手机号添加到系统中。'
    },
    {
      question: '账号添加后多久可以登录？',
      answer: '管理员添加账号后，您可以立即使用微信登录进行账号绑定。'
    },
    {
      question: '忘记密码怎么办？',
      answer: '可以使用短信验证码登录，或联系管理员重置密码。'
    }
  ]
}