// 消息数据标准化工具函数

/**
 * 标准化role字段
 * 将各种可能的role值转换为标准的 'user' 和 'assistant'
 * @param {string|number|null|undefined} role - 原始role值
 * @returns {string} 标准化后的role值 ('user' 或 'assistant')
 */
export function normalizeRole(role) {
  if (!role) return 'assistant' // 默认为AI回复
  
  const roleLower = String(role).toLowerCase()
  
  // 各种可能的用户role值
  const userRoles = ['USER',"user"]
  
  // 各种可能的AI role值
  const aiRoles = ['assistant', "ASSISTANT"]
  
  if (userRoles.includes(roleLower)) {
    return 'user'
  } else if (aiRoles.includes(roleLower)) {
    return 'assistant'
  } else {
    console.warn('Unknown role value:', role, 'defaulting to assistant')
    return 'assistant'
  }
}

/**
 * 验证和标准化消息数据
 * @param {object} msg - 原始消息对象
 * @returns {object|null} 标准化后的消息对象，如果输入无效则返回null
 */
export function normalizeMessage(msg) {
  if (!msg) return null
  
  const normalized = {
    ...msg,
    role: normalizeRole(msg.role),
    content: msg.content || '',
    id: msg.id || msg.sequence || Date.now() + Math.random()
  }
  
  return normalized
}

/**
 * 批量标准化消息数组
 * @param {array} messages - 原始消息数组
 * @returns {array} 标准化后的消息数组
 */
export function normalizeMessages(messages) {
  if (!Array.isArray(messages)) return []
  
  return messages
    .map(msg => normalizeMessage(msg))
    .filter(msg => msg !== null)
}

/**
 * 处理API响应中的消息数据
 * @param {object} apiResponse - API响应对象
 * @returns {array} 标准化后的消息数组
 */
export function extractAndNormalizeMessages(apiResponse) {
  if (!apiResponse || !apiResponse.data) return []
  
  // 处理各种可能的响应数据结构
  const records = apiResponse.data.data?.content || 
                  apiResponse.data.data?.records || 
                  apiResponse.data.content || 
                  apiResponse.data.records || 
                  []



  return normalizeMessages(records)
}

/**
 * 处理AI回复数据
 * @param {object|string} aiResponse - AI回复数据
 * @returns {object} 标准化后的AI消息对象
 */
export function processAIResponse(aiResponse) {
  if (!aiResponse) return null
  
  let content = ''
  let role = 'assistant'
  
  if (typeof aiResponse === 'string') {
    content = aiResponse
  } else if (typeof aiResponse === 'object') {
    content = aiResponse.content || aiResponse.message || JSON.stringify(aiResponse)
    // 如果后端返回了role字段，使用它，否则默认为assistant
    role = aiResponse.role || 'assistant'
  }
  
  const normalizedMessage = normalizeMessage({
    role: role,
    content: content,
    sequence: Date.now() + 1
  })
  return normalizedMessage
}
