# 食物健康管理系统 API 文档

## 概述

本文档描述了食物健康管理系统的所有后端API接口，包括用户认证、AI对话、饮食分析等功能。

### 基础信息

- **基础URL**: `http://localhost:8080`
- **CORS配置**: 允许 `http://localhost:5173`（前端开发服务器）
- **认证方式**: Token认证（在登录/注册后返回）
- **响应格式**: 统一使用 `Result<T>` 格式

### 通用响应格式

所有API接口都返回统一的响应格式：

```json
{
  "code": 200,        // 状态码：200成功，400+错误
  "message": "success", // 响应信息
  "data": {}          // 响应数据，类型根据接口而定
}
```

---

## 1. 用户认证模块 (`/api/auth`)

### 1.1 用户登录

**接口描述**: 用户登录验证，返回认证token

- **请求路径**: `POST /api/auth/login`
- **请求格式**: `application/json`

**请求参数**:
```json
{
  "username": "string",  // 用户名（必填）
  "password": "string"   // 密码（RSA加密后的字符串）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  // JWT token
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null
}
```

### 1.2 用户注册

**接口描述**: 新用户注册

- **请求路径**: `POST /api/auth/register`
- **请求格式**: `application/json`

**请求参数**:
```json
{
  "username": "string",  // 用户名（必填）
  "password": "string",  // 密码（RSA加密后的字符串）
  "email": "string"      // 邮箱（必填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  // JWT token
  }
}
```

### 1.3 用户设置更新

**接口描述**: 更新用户设置信息

- **请求路径**: `POST /api/auth/setting`
- **请求格式**: `application/json`

**请求参数**:
```json
{
  "username": "string",  // 用户名
  "userid": 123,         // 用户ID
  "sick": "string",      // 疾病信息（用|#|分隔）
  "taboo": "string",     // 忌口信息（用|#|分隔）
  "img": "string"        // 头像图片Base64
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "string",  // 用户名
    "sick": "string",      // 疾病信息（用|#|分隔）
    "taboo": "string",     // 忌口信息（用|#|分隔）
    "img": "string"
  }
}
```

### 1.4 用户设置查询

**接口描述**: 查询用户设置信息（主要用于密码修改）

- **请求路径**: `GET /api/auth/setting`
- **请求格式**: `application/json`

**请求参数**:
```json
{
  "userid": 123,           // 用户ID
  "old_password": "string", // 旧密码（RSA加密）
  "new_password": "string"  // 新密码（RSA加密）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "旧密码与原密码不一致或新旧密码一样",
  "data": null
}
```

---

## 2. AI对话模块

### 2.1 对话管理 (`/api/ai/conversation`)

#### 2.1.1 创建对话

**接口描述**: 创建新的AI对话

- **请求路径**: `POST /api/ai/conversation/create`
- **请求格式**: `application/json`

**请求参数**:
```json
{
  "userId": 123,         // 用户ID（必填）
  "title": "string",      // 对话标题
  "create_time": "string" // 创建时间（可选）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 456,                    // 对话ID
    "title": "我的健康咨询",        // 对话标题
    "userId": 123,                // 用户ID
    "create_time": "2024-01-01T10:00:00" // 创建时间
  }
}
```

#### 2.1.2 获取对话列表

**接口描述**: 分页获取用户的对话列表

- **请求路径**: `GET /api/ai/conversation/list`
- **请求格式**: `query string`

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| userId | long | 是   | -      | 用户ID |
| pageNum | int | 否   | 1      | 页码 |
| pageSize | int | 否   | 10     | 每页条数 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [              // 对话列表
      {
        "id": 456,
        "title": "对话1",
        "userId": 123,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T11:00:00"
      }
    ],
    "pageNum": 1,             // 当前页码
    "pageSize": 10,           // 每页条数
    "total": 25,              // 总条数
    "pages": 3                // 总页数
  }
}
```

#### 2.1.3 删除对话

**接口描述**: 删除指定对话

- **请求路径**: `DELETE /api/ai/conversation/delete`
- **请求格式**: `query string`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | long | 是   | 对话ID |
| userId | long | 是   | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 2.2 消息管理 (`/api/ai/message`)

#### 2.2.1 发送消息

**接口描述**: 发送消息并获取AI回复

- **请求路径**: `POST /api/ai/message/send`
- **请求格式**: `application/json`

**请求参数**:
```json
{
  "userId": 123,              // 用户ID（必填）
  "conversationId": 456,      // 对话ID（必填）
  "content": "string",        // 消息内容
  "role": "string",          // 角色（根据function_type不同含义不同）
  "function_type": "string", // 功能类型
  "img": "string",           // 图片base64或零食名称
  "mimeType": "string"       // 图片类型或零食数量
}
```

**功能类型说明**:
| function_type | role参数说明 | img参数说明 | mimeType参数说明 | 功能描述 |
|--------------|-------------|-------------|-----------------|----------|
| `normal` | 无特殊含义 | 图片base64字符串 | 图片MIME类型 | 普通AI对话 |
| `food_analysis` | 餐食类型：`breakfast`/`lunch`/`dinner` | 图片base64字符串 | 图片MIME类型 | 三餐分析 |
| `snack_analysis` | 零食类型：`饮品`/`袋装零食` | 零食名称 | 零食数量（克/毫升） | 零食分析 |
| `medication_reminder` | 无特殊含义 | 无特殊含义 | 无特殊含义 | 用药提醒（特殊处理） |
| `report_analysis` | 无特殊含义 | 图片base64字符串 | 图片MIME类型 | 报告识别（开发中） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 789,                    // 消息ID
    "conversationId": 456,        // 对话ID
    "userId": 123,                // 用户ID
    "content": "AI回复内容",      // AI回复内容
    "role": "assistant",          // 角色
    "sequence": 2,                // 消息序号
    "functionType": "NORMAL",     // 功能类型
    "createTime": "2024-01-01T10:00:00", // 创建时间
    "deleteFlag": 0               // 删除标志
  }
}
```

#### 2.2.2 获取消息列表

**接口描述**: 分页获取对话中的消息列表

- **请求路径**: `GET /api/ai/message/list`
- **请求格式**: `query string`

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| conversationId | long | 是   | -      | 对话ID |
| userId | long | 是   | -      | 用户ID |
| pageNum | int | 否   | 1      | 页码 |
| pageSize | int | 否   | 10     | 每页条数 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [              // 消息列表
      {
        "id": 789,
        "conversationId": 456,
        "userId": 123,
        "content": "用户消息",
        "role": "user",
        "sequence": 1,
        "functionType": "NORMAL",
        "createTime": "2024-01-01T10:00:00",
        "deleteFlag": 0
      },
      {
        "id": 790,
        "conversationId": 456,
        "userId": 123,
        "content": "AI回复内容",
        "role": "assistant",
        "sequence": 2,
        "functionType": "NORMAL",
        "createTime": "2024-01-01T10:01:00",
        "deleteFlag": 0
      }
    ],
    "pageNum": 1,             // 当前页码
    "pageSize": 10,           // 每页条数
    "total": 50,              // 总条数
    "pages": 5                // 总页数
  }
}
```

---

## 3. 功能详细说明

### 3.1 Token认证

所有需要用户身份验证的接口都需要在请求头中携带Token：

```
Authorization: Bearer <token>
```

Token格式：`user_id|#|username|#|avatar_base64` 的Base64编码

### 3.2 功能类型详解

#### 3.2.1 普通AI对话 (`normal`)

- **用途**: 一般性的健康咨询
- **参数说明**:
  - `content`: 用户的问题或描述
  - `img`: 可选，图片的base64编码
  - `mimeType`: 可选，图片的MIME类型（如 `image/jpeg`）

#### 3.2.2 三餐分析 (`food_analysis`)

- **用途**: 分析用户的三餐饮食，提供健康建议
- **参数说明**:
  - `role`: 餐食类型，必须为 `breakfast`、`lunch`、`dinner` 之一
  - `content`: 食物名称描述
  - `img`: 可选，食物图片的base64编码
  - `mimeType`: 可选，图片的MIME类型

**AI分析内容**:
- 结合用户近3天的饮食记录
- 考虑用户的忌口和疾病情况
- 分析当前餐的营养构成
- 提供下一餐的个性化建议

#### 3.2.3 零食分析 (`snack_analysis`)

- **用途**: 分析零食的健康风险等级
- **参数说明**:
  - `role`: 零食类型，必须为 `饮品` 或 `袋装零食`
  - `content`: 零食名称（此参数不使用，实际使用img字段）
  - `img`: 零食名称
  - `mimeType`: 零食数量（克/毫升）

**AI分析内容**:
- 查询零食营养数据库
- 结合用户24小时内的零食记录
- 考虑用户的饮食限制和疾病
- 给出红绿灯等级建议：
  - 🟢 绿灯：低糖、低脂、低钠，天然食材为主
  - 🟡 黄灯：适量食用，注意份量
  - 🔴 红灯：高糖、高脂、高钠，建议严格限制

#### 3.2.4 报告识别 (`report_analysis`)

- **状态**: 开发中
- **用途**: 识别健康报告图片
- **参数说明**:
  - `img`: 报告图片的base64编码
  - `mimeType`: 图片的MIME类型

### 3.3 图片处理

对于支持图片上传的接口，图片处理规则：

1. **图片格式**: 支持 `image/jpeg`、`image/png` 等常见格式
2. **图片大小**: 建议不超过5MB
3. **存储位置**: 服务器保存到 `src/main/resources/img` 目录
4. **返回**: 返回图片的访问URL

### 3.4 错误处理

所有接口的错误响应都遵循统一格式：

```json
{
  "code": 400,          // 错误码
  "message": "错误信息", // 错误描述
  "data": null          // 错误时数据为null
}
```

常见错误码：
- `200`: 成功
- `400`: 参数错误
- `401`: 未授权（Token无效或过期）
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误

---

## 4. 前端集成建议

### 4.1 请求拦截器

建议在axios中添加请求拦截器，统一处理：

```javascript
// 请求拦截器
axios.interceptors.request.use(config => {
  // 添加Token
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token过期，跳转登录页
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 4.2 图片上传

对于需要上传图片的接口，建议使用以下方式：

```javascript
// 图片转base64
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
};

// 使用示例
const file = event.target.files[0];
const base64 = await fileToBase64(file);
const mimeType = file.type;

// 发送请求
const response = await axios.post('/api/ai/message/send', {
  userId: 123,
  conversationId: 456,
  content: '请分析这餐',
  role: 'lunch',
  function_type: 'food_analysis',
  img: base64,
  mimeType: mimeType
});
```

### 4.3 分页处理

分页接口建议使用以下方式处理：

```javascript
// 分页请求
const fetchMessages = async (conversationId, page = 1) => {
  const response = await axios.get('/api/ai/message/list', {
    params: {
      conversationId,
      userId: currentUser.id,
      pageNum: page,
      pageSize: 10
    }
  });
  
  const { content, pageNum, pageSize, total, pages } = response.data.data;
  // 更新UI
};
```

---

## 5. 注意事项

1. **RSA加密**: 用户密码在前端需要使用RSA加密，后端会解密验证，密钥暂时为空
2. **Token管理**: Token需要在前端安全存储，建议使用localStorage
3. **图片大小**: 上传图片时注意控制大小，避免服务器压力过大
4. **错误处理**: 前端需要处理各种错误情况，提供友好的用户提示
5. **并发控制**: 避免频繁发送消息，建议添加防抖或节流机制

---

## 6. 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 支持用户注册登录
- 支持AI对话功能
- 支持三餐分析和零食分析
- 支持图片上传和分析

---

**文档版本**: v1.0.0  
**最后更新**: 2024-01-01  
**维护人员**: 后端开发团队