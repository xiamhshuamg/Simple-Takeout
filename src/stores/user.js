import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getCurrentUser, logout as apiLogout } from "@/api/user";

export const useUserStore = defineStore("user", () => {
    const user = ref(null);
    const token = ref(localStorage.getItem("token") || "");
    const userId = ref(localStorage.getItem("userId") || "");
    const role = ref(localStorage.getItem("role") || "customer");

    const isLoggedIn = computed(() => !!token.value);
    const userName = computed(() => {
        if (user.value) {
            return user.value.userName || user.value.adminName || user.value.name || (role.value === 'business' ? user.value.businessName : "用户");
        }
        return "用户";
    });

    async function fetchUserInfo() {
        // 1. 如果没有 token，直接结束
        if (!token.value) return;

        try {
            const res = await getCurrentUser();
            console.log("用户信息接口返回:", res.data);

            if (res.data) {
                user.value = res.data;

                // 确保角色信息正确存储
                if (res.data.role) {
                    role.value = res.data.role;
                    localStorage.setItem("role", res.data.role);
                }

                // 兼容性处理：确保userName字段存在
                if (!user.value.userName && user.value.name) {
                    user.value.userName = user.value.name;
                }

                console.log("用户信息设置成功:", user.value);
                return true;
            }
        } catch (error) {
            console.warn("获取用户信息失败", error);
            if (error.code === 401 || error.response?.status === 401) {
                logout();
            }
            return false;
        }
    }

    // 添加一个立即更新用户信息的方法
    function updateUserField(field, value) {
        if (user.value) {
            user.value[field] = value;
        }
    }

    // 立即更新整个用户对象
    function setUser(newUser) {
        user.value = newUser;
    }

    function setLoginInfo(data) {
        token.value = data.token;
        userId.value = data.id;
        role.value = data.role;

        localStorage.setItem("token", data.token);
        localStorage.setItem("userId", data.id);
        localStorage.setItem("role", data.role);
    }

    async function logout() {
        try {
            await apiLogout();
        } catch (e) {
            // 忽略登出接口错误
        } finally {
            // 清理本地
            token.value = "";
            userId.value = "";
            user.value = null;
            role.value = "";
            localStorage.clear();
        }
    }

    if (token.value) fetchUserInfo();

    return {
        user,
        token,
        userId,
        role,
        isLoggedIn,
        userName,
        fetchUserInfo,
        updateUserField,
        setUser,
        setLoginInfo,
        logout
    };
});