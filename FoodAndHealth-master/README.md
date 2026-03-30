        
# AAA前端界面 - Vue 3 实现

> 基于 Vue 3 + Vite + Capacitor 构建的响应式 AI 对话界面，应用于安卓应用，支持移动端适配与侧边栏收起功能。

## 🚀 项目简介

本项目是一个模仿“千问”AI 对话界面的 Vue 3 前端应用，主要面向安卓应用开发，采用了Capacitor负责大打包为安卓软件，具备左侧导航栏、主内容区、输入框及多种功能按钮。支持桌面端与移动端自适应，侧边栏可自由收起/展开。

![界面预览](AAAAAA.png) 

## 🧰 技术栈

- **Vue 3** – 核心框架
- **Vite** – 快速构建工具
- **Tailwind CSS** – 响应式样式系统（也可替换为 Element Plus）
- **@vueuse/core** – 响应式工具函数（如 `useMediaQuery`, `useToggle`）
- **Pinia**（可选）– 状态管理（用于控制侧边栏展开状态）

## 📦 项目结构

```
src/
├── components/
│   ├── Sidebar.vue          # 左侧导航栏组件
│   ├── MainContent.vue      # 主内容区组件
│   └── InputBar.vue         # 输入框与功能按钮
├── views/
│   └── Home.vue             # 主页视图
├── store/                   # 状态管理（如 sidebarOpen）
├── assets/
│   └── logo.svg             # AAA Logo
└── App.vue                  # 根组件
```

## 📱 移动端适配说明

- 使用 `@vueuse/core` 的 `useMediaQuery` 检测屏幕宽度
- 当屏幕宽度 < 768px 时，侧边栏默认收起
- 点击左上角“菜单”按钮（汉堡菜单）可展开侧边栏
- 所有按钮和输入框均适配触摸操作

## 💡 功能亮点

- ✅ 侧边栏可收起/展开
- ✅ 响应式布局（PC & Mobile）
- ✅ 清晰的功能分区
- ✅ 支持语音输入、文件上传等扩展功能预留接口

## 🛠️ 自定义配置

在 `store/index.js` 中可以修改侧边栏默认状态：

```js
export const useSidebarStore = defineStore('sidebar', {
  state: () => ({
    isOpen: true // 设置默认是否展开
  }),
  actions: {
    toggle() {
      this.isOpen = !this.isOpen;
    }
  }
});
```
