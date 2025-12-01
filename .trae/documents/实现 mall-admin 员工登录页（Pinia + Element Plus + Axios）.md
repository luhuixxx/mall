## 背景与现状

* 当前 `mall-frontend/mall-admin` 为 uni-app（Vue3），存在 `pages.json`/`manifest.json` 与 `pages/index/index.vue`，未检测到 `package.json`、Pinia、Axios、Element Plus、Auth Store 与登录页。

* 目标：在 admin 前端实现员工登录页，使用 Pinia 保存 token，UI 用 Element Plus，Axios 统一封装，请求网关为 `http://localhost:58100`。

## 技术栈与依赖

* 维持 uni-app 项目结构（H5 端为主要目标），补充前端依赖：

  * `pinia`（状态管理）、`axios`（HTTP）、`element-plus`（UI，仅 H5 使用）、`jsencrypt`（RSA-2048/PKCS#1 v1.5 加密）。

* 初始化/补充 `package.json` 并在 `main.js` 注入 Pinia，按需引入 Element Plus（避免非 H5 端打包）。

## 目录与文件改动

* 新增/更新：

  * `mall-frontend/mall-admin/package.json`（依赖与脚本）。

  * `mall-frontend/mall-admin/pages/login/login.vue`（登录页面）。

  * `mall-frontend/mall-admin/store/auth.js`（Pinia：token/employee/roles/permissions，持久化）。

  * `mall-frontend/mall-admin/utils/request.js`（Axios 实例与拦截器，ApiResponse 统一处理）。

  * `mall-frontend/mall-admin/types/api.js`（`ApiResponse<T>`、`EmployeeLoginResp`、`Employee` 等类型）。

  * `mall-frontend/mall-admin/api/employee.js`（`getPublicKey()`、`loginEmployee()`）。

  * 更新 `mall-frontend/mall-admin/pages.json`（注册登录页，设为启动页；首页在登录后进入）。

  * 更新 `mall-frontend/mall-admin/main.js`（创建 pinia、仅 H5 引入 Element Plus 样式与组件）。

## 接口对接与约定

* 公钥：优先 `GET /api/security/public-key`（白名单），备选 `GET /api/employee/public-key`。

* 登录：`POST /api/employee/login`，`{ username, password: RSA 密文 }`。

* 响应：`ApiResponse<EmployeeLoginResp>`（含 `token/employee/roles/permissions`）。

* 认证：除登录与公钥接口外，Axios 自动附加 `Authorization: Bearer <token>`。

## 登录流程

1. 登录页加载时请求后端公钥（PEM），失败则提示并禁止提交。
2. 表单提交：用 `jsencrypt` 使用公钥对明文密码做 RSA-2048 PKCS#1 v1.5 加密。
3. 调用登录接口，成功后：

   * 将 `token/employee/roles/permissions` 写入 Pinia，并持久化到 `localStorage`（如 `mall_admin_token`）。

   * 设置 Axios 实例默认 `Authorization` 头（`Bearer <token>`）。

   * 跳转首页 `pages/index/index`。
4. 失败时：使用 Element Plus `ElMessage` 显示后端 `code/message`。

## Axios 封装

* `baseURL: http://localhost:58100`，`timeout: 15s`。

* 请求拦截器：从 Pinia 取 token，除白名单接口外附加 `Authorization: Bearer <token>`。

* 响应拦截器：判断 `ApiResponse.code`，非 0 则抛错并提示；`data` 正常返回。

* 错误统一处理：网络错误/超时统一消息提示。

## Pinia Store（auth）

* state：`token`、`employee`、`roles`、`permissions`、`publicKey`。

* actions：`setToken`、`setProfile`、`fetchPublicKey`、`login`、`logout`。

* 持久化：刷新后从 `localStorage` 恢复 token 与用户信息，启动时设置 Axios `Authorization`。

## 路由与访问控制（uni-app）

* `pages.json`：启动页设为 `/pages/login/login`；登录成功后 `uni.reLaunch` 到首页。

* 页面级守卫：在首页等受保护页面的 `onLoad`/`onShow` 中检测 token，缺失则跳转登录。

* 角色/权限用于控制页面按钮显示（后续扩展）。

## UI 设计（Element Plus）

* 登录表单：`ElForm` + `ElInput`（用户名/密码）+ `ElButton` 提交；支持 `loading` 态与基础校验。

* 交互提示：使用 `ElMessage` 成功/失败反馈。

* 仅在 H5 端引入 Element Plus（`process.env.UNI_PLATFORM === 'h5'` 条件加载或构建时处理）。

## 安全与细节

* 仅缓存公钥字符串；不落地私钥。

* 加密算法：`jsencrypt` 默认即 PKCS#1 v1.5，确保使用 2048 位公钥。

* 禁止重复提交（按钮 `loading`/`disabled`），防抖。

* 登录成功后清理明文密码变量。

## 代码草图

* `utils/request.ts`

```ts
import axios from 'axios'
const instance = axios.create({ baseURL: 'http://localhost:58100', timeout: 15000 })
instance.interceptors.request.use(cfg => { /* 从 Pinia 取 token，非白名单则加 Bearer */ return cfg })
instance.interceptors.response.use(res => { /* 统一解包 ApiResponse */ return res.data })
export default instance
```

* `api/employee.ts`

```ts
import request from '@/utils/request'
export const getPublicKey = () => request.get('/api/security/public-key')
export const loginEmployee = (payload) => request.post('/api/employee/login', payload)
```

* `store/auth.ts`

```ts
import { defineStore } from 'pinia'
export const useAuthStore = defineStore('auth', { state: () => ({ token: '', employee: null, roles: [], permissions: [], publicKey: '' }) })
```

* `pages/login/login.vue`

```vue
<template>
  <el-form>...</el-form>
</template>
<script setup>
// 获取公钥 -> 加密密码 -> 调用登录 -> 写入 Pinia -> 跳转首页
</script>
```

## 验证与联调

* 本地 H5 运行：登录页加载公钥成功后提交账号密码（前端加密）。

* 观察后端返回 `token/roles/permissions/employee`，确认写入 Pinia 与页面跳转。

* 进入首页后调用受保护接口（若有），验证 `Authorization: Bearer <token>` 头是否携带。

## 交付项清单

* 新增：`package.json`、`utils/request.ts`、`api/employee.ts`、`store/auth.ts`、`types/api.ts`、`pages/login/login.vue`。

* 更新：`main.js`、`pages.json`（添加登录页并设为启动页）。

## 后续扩展（可选）

* 退出登录（清空 Pinia + 本地存储，返回登录页）。

* 刷新 token 与过期处理。

* 基于 `roles/permissions` 的菜单与按钮级权限控制。

