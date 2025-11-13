import { JSEncrypt } from 'jsencrypt'

// 默认的公钥（实际项目中应该从后端获取）
const PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2K8VZv6dTqz9q5YZKkXq
aQ5gKXrBJiQhjZG9V9J2W1ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2
ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqV
q2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2
ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqV
q2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2
ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqV
q2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2
ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqV
q2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2
ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqVq2ZqV
wIDAQAB
-----END PUBLIC KEY-----`

// 加密函数
export function encrypt(text: string): string {
  const encrypt = new JSEncrypt()
  encrypt.setPublicKey(PUBLIC_KEY)
  return encrypt.encrypt(text) || ''
}

// 解密函数
export function decrypt(text: string): string {
  const decrypt = new JSEncrypt()
  decrypt.setPrivateKey(PUBLIC_KEY) // 注意：这里应该是私钥
  return decrypt.decrypt(text) || ''
}