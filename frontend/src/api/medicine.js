import http from './http'

// 发送消息 — POST /api/medicine/send
// payload: { userId, conversationId, content, role, function_type, img, mimeType }
export const sendMedicineRecord = (payload) =>
    http.post('/api/medicine/send', payload)
