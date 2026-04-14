import JSEncrypt from 'jsencrypt'
import { sm3 } from 'sm-crypto'

// 你项目里的sm3库
const STATIC_PUBLIC_KEY = '-----BEGIN PUBLIC KEY-----\n' +
    'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCuwYGRpfudUQ3f0Z9l1+3V3yYl\n' +
    'Hd7flf4KSe1Z6F9OWS3sLYvoYd1x5ahUHcmImKR6DcKM/GNi3oUaabp7kWZNGSS1\n' +
    'Frbc4zJRqXuP1GgvLYNv3TQwQo8A96AEM72g/leQfN2B1ZTrvMlzJkabkmIxlfQp\n' +
    'J8q1+acaiXh5c8QqxQIDAQAB\n' +
    '-----END PUBLIC KEY-----\n'

// ✅ 导出 RSA 加密
export const encryptPassword = (password) => {
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(STATIC_PUBLIC_KEY)

    const encrypted = encryptor.encrypt(password)

    if (!encrypted) {
        console.error('RSA 加密失败，请检查公钥格式是否正确')
        throw new Error('加密失败')
    }

    return encrypted
}

// ✅ 导出 SM3 加密
export const computeSM3 = (data) => {
    if (typeof data !== 'string') {
        throw new Error('输入必须是字符串')
    }
    return sm3(data)
}
