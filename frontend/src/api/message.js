import http from './http'

// 发送消息 — POST /api/ai/message/send
// payload: { userId, conversationId, content, role, function_type, img, mimeType }
export const sendMessage = (payload) =>
  http.post('/api/ai/message/send', payload)

// 获取消息列表 — GET /api/ai/message/list
export const getMessageList = (conversationId, userId, pageNum = 1, pageSize = 20) =>
  http.get('/api/ai/message/list', { params: { conversationId, userId, pageNum, pageSize } })
