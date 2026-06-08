# 中医药品管理系统 - 前端

> Vue 3 + Element Plus + ECharts 构建的中医药品管理后台前端

---

## 目录

- [1. 技术栈](#1-技术栈)
- [2. 项目结构](#2-项目结构)
- [3. 快速开始](#3-快速开始)
- [4. 架构设计](#4-架构设计)
- [5. 路由设计](#5-路由设计)
- [6. 页面说明](#6-页面说明)
- [7. 主题配色](#7-主题配色)
- [8. API 对接](#8-api-对接)
- [9. 构建部署](#9-构建部署)
- [10. 开发规范](#10-开发规范)

---

## 1. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 前端框架 |
| Vue Router | 4.3+ | 路由管理 |
| Pinia | 2.1+ | 状态管理 |
| Element Plus | 2.5+ | UI 组件库 |
| ECharts | 5.5+ | 图表可视化 |
| Axios | 1.6+ | HTTP 请求 |
| Vite | 5.1+ | 构建工具 |
| Day.js | 1.11+ | 日期处理 |

---

## 2. 项目结构

```
medical-ui/
├── index.html                        # HTML 入口
├── package.json                      # 依赖配置
├── vite.config.js                    # Vite 配置（代理、按需导入）
├── .env.development                  # 开发环境变量
├── .env.production                   # 生产环境变量
├── .gitignore
├── README.md
└── src/
    ├── main.js                       # 应用入口（ElementPlus/i18n/Pinia注册）
    ├── App.vue                       # 根组件
    │
    ├── api/                          # 接口层
    │   ├── request.js                # axios 封装 + JWT 拦截器
    │   ├── auth.js                   # 认证接口（登录/退出/验证码/用户信息）
    │   ├── medicine.js               # 药品接口（CRUD/库存/分类统计）
    │   └── user.js                   # 用户管理接口
    │
    ├── router/
    │   └── index.js                  # 路由配置 + Token 守卫
    │
    ├── stores/
    │   └── user.js                   # Pinia 用户状态（登录/退出/Token管理）
    │
    ├── utils/
    │   └── auth.js                   # localStorage Token 存取工具
    │
    ├── layout/
    │   └── Layout.vue                # 主布局（侧栏+顶栏+内容区）
    │
    ├── styles/
    │   └── global.css                # 全局主题样式（中医配色+CSS变量）
    │
    └── views/
        ├── login/index.vue           # 登录页
        ├── dashboard/index.vue       # 工作台（统计卡片+饼图+预警+快捷操作）
        ├── medicine/
        │   ├── index.vue             # 药品列表（搜索/分页/删除）
        │   ├── form.vue              # 药品新增/编辑表单
        │   └── alert.vue             # 库存预警（补货操作）
        ├── user/
        │   └── index.vue             # 用户管理（列表+新增/编辑弹窗+重置密码）
        └── error/
            └── 404.vue               # 404 页面
```

---

## 3. 快速开始

### 前置条件

- Node.js >= 16
- 后端服务已启动（默认 `http://localhost:8080`）

### 本地开发

```bash
# 1. 安装依赖
cd medical-ui
npm install

# 2. 启动开发服务器
npm run dev

# 3. 访问
# http://localhost:3000
```

开发服务器已配置代理，`/api` 请求自动转发到后端 `http://localhost:8080`。

### 修改后端地址

编辑 `.env.development`：

```env
VITE_API_BASE = /api       # 开发环境通过 Vite 代理
```

或者直连后端（不经过代理，需处理后端 CORS）：

```env
VITE_API_BASE = http://192.168.1.100:8080/api
```

---

## 4. 架构设计

### 4.1 请求流程

```
用户操作 → Vue 组件 → API 模块 → axios 实例
                                    │
                          ┌─────────┴─────────┐
                          │  请求拦截器         │
                          │  自动附加 Token     │
                          │  Authorization:     │
                          │  Bearer xxx         │
                          └─────────┬─────────┘
                                    │
                                    ▼
                              后端 API
                                    │
                          ┌─────────┴─────────┐
                          │  响应拦截器                  │
                          │  1. 自动刷新 Token（响应头）   │
                          │  2. 401 → 清除 Token → 跳登录  │
                          │  3. 统一错误提示               │
                          └─────────┬─────────┘
                                    │
                                    ▼
                              Vue 组件渲染
```

### 4.2 Token 管理

```javascript
// 存储：localStorage
localStorage.setItem('medical_token', token)

// 读取：axios 请求拦截器自动附加
config.headers.Authorization = `Bearer ${token}`

// 刷新：响应头检测
if (newToken) setToken(newToken)

// 失效：401 时自动清除 + 跳转登录页
```

### 4.3 路由守卫

```
访问任意页面
    │
    ├─ /login？
    │   ├─ 已有 Token → 跳转工作台
    │   └─ 无 Token → 显示登录页
    │
    └─ 其他页面？
        ├─ 有 Token → 正常访问
        └─ 无 Token → 跳转 /login?redirect=原路径
```

### 4.4 状态管理（Pinia）

```
userStore
├── state
│   ├── token          # JWT Token
│   └── userInfo       # 当前用户信息
├── actions
│   ├── login()        # 登录 → 存储 Token + 用户信息
│   ├── logout()       # 退出 → 清除 Token + 用户信息
│   └── fetchUserInfo()# 获取用户信息
```

---

## 5. 路由设计

| 路径 | 页面 | 权限 | 说明 |
|------|------|------|------|
| `/login` | 登录页 | 公开 | — |
| `/dashboard` | 工作台 | 登录即可 | 统计卡片、分类饼图、库存预警 |
| `/medicine` | 药品列表 | 登录即可 | 搜索、分页、删除 |
| `/medicine/form` | 药品表单 | 登录即可 | 新增/编辑（侧栏隐藏） |
| `/stock-alert` | 库存预警 | 登录即可 | 预警列表、补货操作 |
| `/user` | 用户管理 | 登录即可 | 增删改查、重置密码 |
| `/404` | 404 | 公开 | 页面不存在 |

---

## 6. 页面说明

### 6.1 登录页

分左右两栏设计：
- **左半屏**：深棕渐变背景 + 品牌区（🌿 图标 + 系统名称 + 宣传语）
- **右半屏**：暖白底 + 登录表单卡片（用户名/密码/验证码/登录按钮）

验证码为 4 位字符图形验证码（数字+字母混合），点击刷新。

### 6.2 工作台

四块统计卡片 + 分类饼图 + 库存预警表格 + 快捷操作区：
- **统计卡片**：药品总数、本月新增、库存预警、今日处方（从后端实时读取）
- **分类饼图**：ECharts 玫瑰饼图，调用 `/api/medicine/stats/category` 获取真实数据
- **库存预警表**：显示当前库存低于阈值的药品列表
- **快捷操作**：hover 变色 + 上浮动效，点击跳转对应页面

### 6.3 药品管理

- 搜索栏：药品名称（模糊）、性味（下拉）、状态（上架/下架）
- 表格列：名称、编码、分类、性味、产地、库存、状态、操作
- 新增/编辑：弹窗表单，包含完整的中医特色字段（性味归经、功效主治、毒性、禁忌等）
- 库存管理：支持出入仓操作

### 6.4 库存预警

- 红色高亮显示当前库存量
- 显示缺口数量（预警阈值 - 当前库存）
- 一键补货弹窗（输入数量确认）

### 6.5 用户管理

- 用户列表（用户名、真实姓名、手机号、邮箱、状态、最后登录）
- 新增用户：用户名/真实姓名/手机号/邮箱/角色/状态，**默认密码 123456**
- 编辑用户：同上
- 重置密码：一键重置为 123456，该用户 Token 同时失效

---

## 7. 主题配色

采用中医风格暖色调：

```css
主色调       #8B5E3C （暖棕）
强调色       #C9A96E （古铜金）
危险色       #C04040 （朱砂红）
成功色       #6B8E23 （橄榄绿）
警告色       #D4A017 （琥珀黄）
背景色       #F5F0E8 （宣纸暖白）
卡片背景     #FFFAF5
侧栏渐变     #3E2723 → #5C3D1E
```

所有颜色通过 CSS 变量定义在 `src/styles/global.css`，覆盖 Element Plus 默认主题。

---

## 8. API 对接

### 8.1 接口映射

| 前端 API 模块 | 方法 | 后端接口 | 说明 |
|---------------|------|----------|------|
| `auth.js` | `getCaptcha()` | `GET /api/auth/captcha` | 获取验证码 |
| `auth.js` | `login(data)` | `POST /api/auth/login` | 登录 |
| `auth.js` | `logout()` | `POST /api/auth/logout` | 退出 |
| `auth.js` | `getUserInfo()` | `GET /api/auth/userInfo` | 用户信息 |
| `medicine.js` | `getMedicinePage(params)` | `GET /api/medicine/page` | 药品分页 |
| `medicine.js` | `getMedicineById(id)` | `GET /api/medicine/{id}` | 药品详情 |
| `medicine.js` | `addMedicine(data)` | `POST /api/medicine` | 新增药品 |
| `medicine.js` | `updateMedicine(data)` | `PUT /api/medicine` | 更新药品 |
| `medicine.js` | `deleteMedicine(id)` | `DELETE /api/medicine/{id}` | 删除药品 |
| `medicine.js` | `updateStock(id, qty)` | `PUT /api/medicine/stock/{id}` | 更新库存 |
| `medicine.js` | `getStockAlert(params)` | `GET /api/medicine/alert` | 库存预警 |
| `medicine.js` | `countByCategory()` | `GET /api/medicine/stats/category` | 分类统计 |
| `user.js` | `getUserPage(params)` | `GET /api/user/page` | 用户分页 |
| `user.js` | `addUser(data)` | `POST /api/user` | 新增用户 |
| `user.js` | `updateUser(data)` | `PUT /api/user` | 更新用户 |
| `user.js` | `deleteUser(id)` | `DELETE /api/user/{id}` | 删除用户 |
| `user.js` | `resetPassword(id)` | `PUT /api/user/resetPassword/{id}` | 重置密码 |

### 8.2 雪花 ID 精度处理

后端使用雪花算法生成 19 位 Long 型 ID，前端 JavaScript 无法精确表示超过 16 位的整数。

**解决方案**：后端 `BaseEntity.id` 添加 `@JsonFormat(shape = JsonFormat.Shape.STRING)`，ID 在 JSON 中以字符串形式传输，前端无需额外处理。

### 8.3 Token 自动续期

后端在 Token 剩余有效期不足 1 小时时，会在响应头 `Authorization` 中返回新 Token。前端 `request.js` 响应拦截器自动检测并更新 `localStorage`。

---

## 9. 构建部署

### 9.1 生产构建

```bash
npm run build
```

产物输出到 `dist/` 目录。

### 9.2 Nginx 部署

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    root /var/www/medical-ui/dist;
    index index.html;

    # Vue Router history 模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 9.3 Docker 部署

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### 9.4 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE` | 后端 API 地址 | `/api`（开发）/ `https://your-domain.com/api`（生产） |

---

## 10. 开发规范

### 10.1 组件命名

- 页面组件：`views/模块名/` 下，使用小写
- 公共组件：`components/` 下，使用 PascalCase

### 10.2 API 模块

- 每个业务模块一个 API 文件（`api/xxx.js`）
- 统一使用 `request.js` 导出的 axios 实例
- 接口方法命名使用动词开头：`getXxx`、`addXxx`、`updateXxx`、`deleteXxx`

### 10.3 样式

- 全局主题变量在 `styles/global.css`
- 页面内部样式使用 `<style scoped>`
- 颜色使用 CSS 变量而非硬编码（如 `var(--primary)`）

### 10.4 新增页面步骤

1. 在 `views/` 下创建 `.vue` 文件
2. 在 `router/index.js` 中添加路由
3. 在 `layout/Layout.vue` 中添加菜单项（如需要）
4. 在 `api/` 下添加接口文件（如需要）

---

> 📧 配套后端项目：`https://github.com/mytortoisegit/medical-system`
