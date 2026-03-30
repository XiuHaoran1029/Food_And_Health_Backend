import http from './http'

// 创建对话 — POST /api/ai/conversation/create
export const createConversation = (userId, title) =>
  http.post('/api/ai/conversation/create', { userId, title })

// 获取对话列表 — GET /api/ai/conversation/list
export const getConversationList = (userId, pageNum = 1, pageSize = 20) =>
  http.get('/api/ai/conversation/list', { params: { userId, pageNum, pageSize } })

// 删除对话 — DELETE /api/ai/conversation/delete
export const deleteConversation = (conversationId, userId) =>
  http.delete('/api/ai/conversation/delete', { params: { conversationId, userId } })
