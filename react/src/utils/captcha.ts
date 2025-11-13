// 生成演示验证码图片
export function generateDemoCaptcha(): { img: string; uuid: string } {
  const canvas = document.createElement('canvas')
  canvas.width = 120
  canvas.height = 40
  const ctx = canvas.getContext('2d')!

  // 生成随机验证码文本
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }

  // 生成UUID
  const uuid = 'demo-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9)

  // 绘制背景
  ctx.fillStyle = '#f0f0f0'
  ctx.fillRect(0, 0, canvas.width, canvas.height)

  // 绘制干扰线
  for (let i = 0; i < 5; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 0.3)`
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.stroke()
  }

  // 绘制验证码文本
  ctx.font = 'bold 24px Arial'
  ctx.fillStyle = '#333'
  ctx.textBaseline = 'middle'
  ctx.textAlign = 'center'

  // 绘制每个字符，随机旋转和位置
  for (let i = 0; i < code.length; i++) {
    ctx.save()
    const x = 20 + i * 25
    const y = 20 + Math.random() * 10 - 5
    ctx.translate(x, y)
    ctx.rotate((Math.random() - 0.5) * 0.4)
    ctx.fillText(code[i], 0, 0)
    ctx.restore()
  }

  // 绘制干扰点
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 0.5)`
    ctx.beginPath()
    ctx.arc(Math.random() * canvas.width, Math.random() * canvas.height, 1, 0, 2 * Math.PI)
    ctx.fill()
  }

  // 转换为base64
  const img = canvas.toDataURL('image/png')

  // 将验证码文本存储到sessionStorage中，用于验证
  sessionStorage.setItem('demo-captcha-code', code)

  return { img, uuid }
}

// 验证演示验证码
export function validateDemoCaptcha(inputCode: string): boolean {
  const storedCode = sessionStorage.getItem('demo-captcha-code')
  return storedCode ? storedCode.toUpperCase() === inputCode.toUpperCase() : false
}