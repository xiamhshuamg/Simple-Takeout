import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import axios from "axios";

import App from "./App.vue";
import router from "./router";
import "./styles/base.css"; // 全局基础样式

const app = createApp(App);
app.provide("$axios",axios);

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

app.mount("#app");

// // ================== 【测试专用】伪造登录状态 START ==================
// // 1. 设置伪造的 Token (后端验证会失败，但前端路由会放行)
// if (!localStorage.getItem("token")) {
//     localStorage.setItem("token", "test-fake-token-123456");
// }
//
// // 2. 设置测试的角色：修改这里的值来测试不同权限 ('customer', 'business', 'admin')
// if (!localStorage.getItem("role")) {
//     localStorage.setItem("role", "customer");
// }
//
// // 3. 设置用户ID
// if (!localStorage.getItem("userId")) {
//     localStorage.setItem("userId", "1");
// }
