# 时康日记项目介绍

时康日记是一个前后端分离的健康管理应用，面向 Web 和 Android（Capacitor 容器）双端。项目以饮食与用药场景为核心，结合 AI 对话、三餐分析、零食分析、体检报告分析等能力，提供移动优先的一体化健康管理体验。

## 1. 项目定位

- 核心目标：把“记录 + 分析 +提醒”串成一个完整链路，而不是单一聊天或单一打卡工具。
- 主要用户：有饮食管理、规律用药、健康复盘需求的普通用户。
- 交互特征：优先适配手机端，针对 Android WebView 做了触摸与滚动优化。

## 2. 已实现功能

### 2.1 AI 多轮会话

- 支持创建、删除、分页查询会话。
- 支持按会话加载历史消息，并在前端进行流式展示体验。
- 后端根据 `function_type` 区分普通问答、三餐分析、零食分析、报告分析。

### 2.2 三餐智能分析

- 前端支持拍照或相册选图、图片压缩后上传。
- 后端结合用户近三天饮食记录、疾病与忌口信息，生成下一餐建议。
- 分析结果与餐次记录持久化，供日历模块复盘展示。

### 2.3 零食智能分析

- 记录零食名称、类型（饮品/袋装零食）、数量、备注。
- 后端可结合营养库数据（若命中）+ 最近 24 小时零食记录给出建议。
- 结果会保存到零食记录表，供日历详情页展示。
f
### 2.4 用药提醒

- 支持录入药名、每日次数、单次剂量、截止日期。
- 数据写入后端后，前端在 Android 端尝试同步系统日历（需权限）。
- 当系统权限或插件调用失败时，页面仍可保存业务数据并给出提示。

### 2.5 饮食日历追溯

- 月视图展示有记录日期。
- 点击日期可查看早餐/午餐/晚餐 + 零食明细。
- 建议内容支持 Markdown 渲染，便于移动端阅读。

### 2.6 体检报告分析

- 通过相机/相册获取报告图片后上传。
- 调用视觉模型进行文本化解读，并回写到会话消息流中。

### 2.7 个人信息与账号设置

- 支持用户名、疾病、忌口、头像修改。
- 支持密码修改与退出登录。

### 2.8 登录态与基础安全处理

- 前端使用本地存储保存 token，并在请求拦截器中自动携带 `Authorization`。
- 登录/注册密码经过前端 `SM3` 后再做 `RSA` 加密传输；后端做 RSA 解密后校验。

## 3. 技术架构

### 3.1 前端

- 框架：Vue 3 + Vite 7
- 状态管理：Pinia
- 路由：Vue Router
- 网络层：Axios（含请求/响应拦截器）
- UI 与样式：Vant + Tailwind CSS + lucide-vue-next
- 跨端能力：Capacitor（Camera、Calendar 等插件）
- 内容渲染：markdown-it

### 3.2 后端

- 框架：Spring Boot（Web、Validation、JPA、Security、Actuator）
- 数据库：MySQL（JPA Repository）
- AI 调用：Java HttpClient 对接火山引擎 Ark 接口（文本与视觉）
- 日志：SLF4J + Logback

### 3.3 业务分层

- Controller：接口入参、响应封装
- Service：核心业务（会话、消息、三餐、零食、用药、用户）
- Repository：数据库访问
- Entity/DTO：领域模型与请求响应对象

## 4. 项目结构

### 4.1 前端（`frontend/src`）

```text
src/
├── api/         # 接口封装：auth/conversation/message/meal/medicine
├── components/  # 业务组件：聊天区、三餐、零食、用药、设置等
├── router/      # 路由与登录拦截
├── store/       # Pinia 状态（用户、侧边栏）
├── styles/      # Android WebView 适配样式
├── utils/       # 加密、图片处理、消息标准化、报告分析工具
├── views/       # 页面视图（Home）
└── main.js      # 入口初始化
```

### 4.2 后端（`src/main/java/org/example/food_a`）

```text
├── controller/  # 认证、会话、消息、三餐、用药接口
├── service/     # 业务实现与 AI 调用
├── repository/  # JPA 数据访问层
├── entity/      # 实体对象
├── dto/         # 请求/响应 DTO
└── common/      # 通用能力：返回体、配置、Token、RSA 解密等
```

## 5.使用说明

### 5.1

使用模拟器或安卓手机运行resources目录下的`时康日记_v1.0_1_release.apk`，进行安装并体验。

## 6. 部署说明

### 6.1 后端

```bash
./mvnw spring-boot:run
```

Windows 可使用：

```powershell
.\mvnw.cmd spring-boot:run
```

### 6.2 前端（Web）

```bash
cd FoodAndHealth-master
npm install
npm run dev
```

### 6.3 Android（Capacitor）

- 在前端完成构建后，同步到 Android 工程。
- 通过 Android Studio 打开 `FoodAndHealth-master/android` 进行真机调试与打包。

## 7. 应用价值

时康日记将 AI 分析能力与日常健康行为管理打通：

- 对用户：降低健康记录成本，提高饮食与用药管理连续性。
- 对产品：具备清晰模块边界，便于逐步扩展慢病管理、家庭健康档案等能力。
- 对工程：前后端结构明确，适合团队协作和后续维护交接。


