# 食物健康管理系统 API 文档（整合完整版）
**文档版本**：v1.0.0
**最后更新**：2026-03-31
**基础URL**：`http://localhost:8080`
**跨域允许**：`http://localhost:5173`
**认证方式**：Token 认证
**统一响应**：`Result<T>` 格式

---

## 目录
1. 通用规范
2. 用户认证模块
3. AI 对话模块
4. 三餐记录日历模块
5. 功能说明与集成建议
6. 错误码与注意事项

---

# 一、通用规范
## 1.1 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```
- `code=200` 成功，`400+` 为异常
- 日历接口兼容 `code=0` 成功、`code=-1` 失败

## 1.2 认证请求头
需身份验证的接口必须携带：
```
Authorization: Bearer <token>
```
Token 格式：`Base64(UserId)|#|Base64(UserName)`

## 1.3 密码规则
登录/注册/改密的密码均为 **RSA 加密字符串**

---

# 二、用户认证模块 `/api/auth`
## 2.1 用户登录
- **POST** `/api/auth/login`
- 请求：`application/json`
```json
{
  "email": "string",
  "password": "string"
}
```
- 成功返回 token
```json
{
  "code": 200,
  "message": "success",
  "data": { "token": "..." }
}
```

## 2.2 用户注册
- **POST** `/api/auth/register`
```json
{
  "username": "string",
  "password": "string",
  "email": "string"
}
```
- 成功返回 token

## 2.3 更新用户设置
- **POST** `/api/auth/setting`
```json
{
  "username": "string",
  "userid": 123,
  "sick": [],
  "taboo": [],
  "img": "base64字符串"
}
```
- 返回更新后用户信息（含头像 URL）

## 2.4 修改密码（查询用户设置）
- **GET** `/api/auth/setting`
```json
{
  "userid": 0,
  "old_password": "string",
  "new_password": "string"
}
```
- 错误：旧密码错误或新旧相同

## 2.5 查询用户信息
- **GET** `/api/auth/info`
- 仅携带 Authorization 头
- 返回：`userid、username、sick、taboo、img`

---

# 三、AI 对话模块
## 3.1 对话管理 `/api/ai/conversation`
### 3.1.1 创建对话
- **POST** `/api/ai/conversation/create`
```json
{
  "userId": 123,
  "title": "string",
  "create_time": "string"
}
```

### 3.1.2 获取对话列表
- **GET** `/api/ai/conversation/list`
- 参数：`userId、pageNum、pageSize`
- 返回分页对话列表（含总条数、总页数）

### 3.1.3 删除对话
- **DELETE** `/api/ai/conversation/delete`
- 参数：`conversationId、userId`

## 3.2 消息管理 `/api/ai/message`
### 3.2.1 发送消息（AI 回复）
- **POST** `/api/ai/message/send`
```json
{
  "userId": 123,
  "conversationId": 456,
  "content": "string",
  "role": "string",
  "function_type": "string",
  "img": "string",
  "mimeType": "string"
}
```

### function_type 说明
| 类型 | role | img | mimeType | 用途 |
|------|------|-----|----------|------|
| normal | - | base64 | 图片类型 | 普通对话 |
| food_analysis | breakfast/lunch/dinner | base64 | 图片类型 | 三餐分析 |
| snack_analysis | 饮品/袋装零食 | 零食名 | 数量 | 零食分析 |
| report_analysis | - | base64 | 图片类型 | 报告识别 |

### 3.2.2 获取消息列表
- **GET** `/api/ai/message/list`
- 参数：`conversationId、userId、pageNum、pageSize`
- 返回分页消息（用户/AI 对话记录，含图片 URL）

---

# 四、三餐记录日历模块 `/api/meal`
## 4.1 按年月查询（日历标记）
- **GET** `/api/meal/month`
- 参数：`year、month,userId`
- 返回当月有饮食记录的日期列表（前端标绿）
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "year": 2026,
    "month": 3,
    "recordDays": ["2026-03-01","2026-03-05"]
  }
}
```

## 4.2 按日期查询当日详情
- **GET** `/api/meal/day`
- 参数：`date=yyyy-MM-dd，userId`
- 返回早餐、午餐、晚餐、零食记录
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "date": "2026-03-01",
    "breakfast": { "imageUrl":"", "comment":"", "suggest":"" },
    "lunch": {...},
    "dinner": {...},
    "snack": [
      {
        "snack_name":"可乐",
        "remark":"冰镇",
        "role":0,
        "count":500.0
      }
    ]
  }
}
```
- `role=0` 饮品，`role=1` 袋装零食

## 4.3 日历模块统一错误
```json
{
  "code": -1,
  "msg": "查询失败/无记录",
  "data": null
}
```

---

# 五、功能与集成说明
## 5.1 图片处理
- 格式：JPEG/PNG
- 大小：≤10MB
- 上传：Base64 提交
- 存储：`src/main/resources/img`
- 返回：可访问图片 URL

## 5.2 零食健康等级
- 🟢 绿灯：低糖低脂低钠
- 🟡 黄灯：适量食用
- 🔴 红灯：高糖高脂高钠，建议限制

## 5.3 前端建议
- 请求拦截器统一携带 Token
- 响应拦截器处理 401 跳转登录
- 图片使用 `FileReader` 转 Base64
- 分页接口统一处理 `pageNum/pageSize`

---

# 六、错误码与注意事项
## 6.1 通用错误码
- `200` 成功
- `400` 参数错误
- `401` 未授权/Token 失效
- `403` 无权限
- `404` 资源不存在
- `500` 服务器异常

## 6.2 注意事项
1. 密码必须前端 RSA 加密
2. Token 建议存在 localStorage
3. 图片避免过大导致上传失败
4. 消息发送增加防抖/节流
5. 日历接口 `code=0/-1`，与主系统 `200/400` 兼容

---
