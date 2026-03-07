import axios from "axios";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import router from "@/router";

export const http = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 10000,
});

// 请求拦截
http.interceptors.request.use(
    (config) => {
        const userStore = useUserStore();
        if (userStore.token) {
            config.headers = config.headers || {};
            config.headers.Authorization = `Bearer ${userStore.token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 响应拦截
http.interceptors.response.use(
    (res) => {
        const data = res.data;
        if (typeof data === "object" && data !== null && "code" in data) {
            if (data.code === 0) {
                return data;
            }
            ElMessage.error(data.msg || "请求失败");
            return Promise.reject(data);
        }
        return data;
    },
    (err) => {
        //处理 401 未授权
        if (err.response && err.response.status === 401) {
            const userStore = useUserStore();
            userStore.logout(); // 清除 store 和 localStorage
            router.push("/login"); // 强制跳回登录页
            ElMessage.error("登录已过期，请重新登录");
            return Promise.reject(err);
        }
        // ---------------------------

        const msg = err?.response?.data?.msg || err?.response?.statusText || err.message || "网络异常";
        ElMessage.error(msg);
        return Promise.reject(err);
    }
);