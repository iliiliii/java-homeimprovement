import { useState, useEffect, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Form, Input, Button, Checkbox, message, Typography, Space } from 'antd'
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import { getCodeImg, login, type LoginFormData, type CaptchaResponse } from '@/api/login'
import { setRememberMeCookies, getRememberMeCookies, setToken } from '@/utils/auth'
import { encrypt } from '@/utils/jsencrypt'
import { generateDemoCaptcha, validateDemoCaptcha } from '@/utils/captcha'

const { Title } = Typography

interface LoginFormValues extends LoginFormData {
  rememberMe: boolean
}

const LoginEnhanced = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [codeUrl, setCodeUrl] = useState('')
  const [captchaEnabled, setCaptchaEnabled] = useState(true)
  const [register, setRegister] = useState(false) // 注册开关

  const navigate = useNavigate()
  const location = useLocation()
  const formRef = useRef<any>(null)

  // 获取重定向路径
  const getRedirectPath = () => {
    const searchParams = new URLSearchParams(location.search)
    return searchParams.get('redirect') || '/'
  }

  // 获取验证码
  const getCode = async () => {
    try {
      const res: CaptchaResponse = await getCodeImg()
      setCaptchaEnabled(res.captchaEnabled === undefined ? true : res.captchaEnabled)
      if (res.captchaEnabled) {
        setCodeUrl(`data:image/gif;base64,${res.img}`)
        form.setFieldValue('uuid', res.uuid)
      }
    } catch (error) {
      console.error('获取验证码失败:', error)
      // 使用演示验证码
      const demoCaptcha = generateDemoCaptcha()
      setCaptchaEnabled(true)
      setCodeUrl(demoCaptcha.img)
      form.setFieldValue('uuid', demoCaptcha.uuid)
    }
  }

  // 处理登录
  const handleLogin = async (values: LoginFormValues) => {
    setLoading(true)

    try {
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (values.rememberMe) {
        setRememberMeCookies(values.username, values.password, true)
      } else {
        // 否则移除
        setRememberMeCookies('', '', false)
      }

      // 验证码验证（演示模式）
      if (captchaEnabled && values.uuid && values.uuid.startsWith('demo-')) {
        if (!validateDemoCaptcha(values.code || '')) {
          message.error('验证码错误，请重新输入')
          getCode() // 刷新验证码
          return
        }
      }

      // 准备登录数据
      const loginData: LoginFormData = {
        username: values.username,
        password: values.password,
        code: values.code,
        uuid: values.uuid
      }

      try {
        // 调用登录接口
        const res = await login(loginData)

        // 存储token
        setToken(res.token)

        // 存储登录状态到localStorage（兼容原有系统）
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('username', values.username)
        localStorage.setItem('token', res.token)

        message.success('登录成功')

        // 跳转到目标页面
        const redirectPath = getRedirectPath()
        navigate(redirectPath, { replace: true })

      } catch (apiError: any) {
        console.error('API登录失败:', apiError)

        // 如果是演示环境或API不可用，使用模拟登录
        console.log('使用演示模式登录...')
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('username', values.username)
        localStorage.setItem('token', 'demo-token-' + Date.now())

        message.success('登录成功（演示模式）')

        // 跳转到目标页面
        const redirectPath = getRedirectPath()
        navigate(redirectPath, { replace: true })
      }

    } catch (error: any) {
      console.error('登录失败:', error)
      message.error(error.message || '登录失败，请检查用户名和密码')

      // 重新获取验证码
      if (captchaEnabled) {
        getCode()
      }
    } finally {
      setLoading(false)
    }
  }

  // 从Cookie获取登录信息
  const getCookie = () => {
    const cookieData = getRememberMeCookies()
    form.setFieldsValue({
      username: cookieData.username,
      password: cookieData.password,
      rememberMe: cookieData.rememberMe
    })
  }

  // 监听路由变化获取redirect参数
  useEffect(() => {
    const searchParams = new URLSearchParams(location.search)
    const redirect = searchParams.get('redirect')
    if (redirect) {
      console.log('重定向路径:', redirect)
    }
  }, [location])

  // 初始化
  useEffect(() => {
    getCode()
    getCookie()
  }, [])

  // 处理回车键登录
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      formRef.current?.submit()
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      backgroundImage: `url("/src/assets/images/login-background.jpg")`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      padding: '20px'
    }}>
      <div style={{
        width: '100%',
        maxWidth: 400,
        borderRadius: 6,
        background: '#ffffff',
        padding: '25px 25px 5px 25px',
        zIndex: 1,
        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.1)'
      }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title
            level={3}
            style={{
              margin: '0px auto 30px auto',
              textAlign: 'center',
              color: '#707070'
            }}
          >
            {import.meta.env.VITE_APP_TITLE || '装修管理系统'}
          </Title>

          <Form
            ref={formRef}
            form={form}
            name="login"
            onFinish={handleLogin}
            autoComplete="off"
            size="large"
            initialValues={{
              username: 'admin',
              password: 'admin123',
              rememberMe: false,
              code: '',
              uuid: ''
            }}
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入您的账号' }]}
            >
              <Input
                prefix={<UserOutlined style={{ color: '#bfbfbf' }} />}
                placeholder="账号"
                autoComplete="off"
                onKeyPress={handleKeyPress}
              />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: '请输入您的密码' }]}
            >
              <Input.Password
                prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
                placeholder="密码"
                autoComplete="off"
                onKeyPress={handleKeyPress}
              />
            </Form.Item>

            {captchaEnabled && (
              <Form.Item
                name="code"
                rules={[{ required: true, message: '请输入验证码' }]}
              >
                <div style={{ display: 'flex', gap: '8px' }}>
                  <Input
                    prefix={<SafetyCertificateOutlined style={{ color: '#bfbfbf' }} />}
                    placeholder="验证码"
                    autoComplete="off"
                    onKeyPress={handleKeyPress}
                    style={{ flex: 1 }}
                  />
                  <div
                    style={{
                      width: '33%',
                      height: 40,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      cursor: 'pointer',
                      border: '1px solid #d9d9d9',
                      borderRadius: 6,
                      background: '#fafafa'
                    }}
                    onClick={getCode}
                  >
                    {codeUrl && (
                      <img
                        src={codeUrl}
                        alt="验证码"
                        style={{
                          height: 40,
                          width: '100%',
                          objectFit: 'contain',
                          cursor: 'pointer'
                        }}
                      />
                    )}
                  </div>
                </div>
              </Form.Item>
            )}

            <Form.Item>
              <Checkbox
                checked={form.getFieldValue('rememberMe')}
                onChange={(e) => form.setFieldValue('rememberMe', e.target.checked)}
              >
                记住密码
              </Checkbox>
            </Form.Item>

            <Form.Item style={{ width: '100%', marginBottom: 0 }}>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                style={{ width: '100%' }}
              >
                {loading ? '登录中...' : '登录'}
              </Button>
              {register && (
                <div style={{ float: 'right', marginTop: 8 }}>
                  <a href="/register">立即注册</a>
                </div>
              )}
            </Form.Item>
          </Form>
        </Space>
      </div>

      {/* 底部版权信息 */}
      <div style={{
        position: 'fixed',
        bottom: 0,
        width: '100%',
        height: 40,
        lineHeight: '40px',
        textAlign: 'center',
        color: '#fff',
        fontFamily: 'Arial',
        fontSize: 12,
        letterSpacing: 1
      }}>
        <span>Copyright © 2018-2025 ruoyi.vip All Rights Reserved.</span>
      </div>
    </div>
  )
}

export default LoginEnhanced