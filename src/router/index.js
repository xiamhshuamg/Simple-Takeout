import { createRouter, createWebHashHistory } from "vue-router";
import { useUserStore } from "@/stores/user"; // 引入 store

// 页面组件
import Home from "@/views/Home.vue";
import BusinessList from "@/views/BusinessList.vue";
import BusinessInfo from "@/views/BusinessInfo.vue";
import ConfirmOrder from "@/views/ConfirmOrder.vue";
import PayOnline from "@/views/PayOnline.vue";
import MyOrders from "@/views/MyOrders.vue";
import UserLogin from "@/views/UserLogin.vue";
import UserRegister from "@/views/UserRegister.vue";
import UserProfile from "@/views/UserProfile.vue";
import AddressList from "@/views/AddressList.vue";
import LocationSelect from "@/views/LocationSelect.vue";
import BusinessEdit from "@/views/business/BusinessEdit.vue";
import FoodManage from "@/views/business/FoodManage.vue";
import OrderHandle from "@/views/business/OrderHandle.vue";
import AdminPanel from "@/views/AdminPanel.vue";

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        { path: "/", redirect: "/home" },

        // 业务页面
        { path: "/home", component: Home },
        { path: "/business-list", component: BusinessList },

        //这里添加 meta: { hideTab: true }，进入商家详情页隐藏底部导航
        { path: "/business/:id", component: BusinessInfo, meta: { hideTab: true } },

        { path: "/confirm-order", component: ConfirmOrder, meta: { requiredRoles: ["customer"], hideTab: true } }, // 结算页也建议隐藏
        { path: "/pay-online", component: PayOnline, meta: { requiredRoles: ["customer"], hideTab: true } },

        { path: "/orders", component: MyOrders, meta: { requiredRoles: ["customer"] } },
        { path: "/profile", component: UserProfile }, // “我的”
        {
            path: "/location",
            component: LocationSelect,
            meta: { hideTab: true, requiredRoles: ["customer"] },
        },
        { path: "/business/edit", component: BusinessEdit, meta: { requiredRoles: ["business"] } },
        { path: "/business/food-manage", component: FoodManage, meta: { requiredRoles: ["business"] } },
        { path: "/business/order-handle", component: OrderHandle, meta: { requiredRoles: ["business"] } },
        { path: "/address-list", component: AddressList, meta: { requiredRoles: ["customer"], hideTab: true } },

        // 管理员页面
        { path: "/admin", component: AdminPanel, meta: { hideTab: true, requiredRoles: ["admin"] } },

        // 登录注册页
        {
            path: "/login",
            component: UserLogin,
            meta: { hideTab: true, isPublic: true },
        },
        {
            path: "/register",
            component: UserRegister,
            meta: { hideTab: true, isPublic: true },
        },
    ],
});

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore();
    const token = localStorage.getItem("token");
    const role = userStore.role || localStorage.getItem("role") || "customer";

    if (to.meta.isPublic) {
        next();
        return;
    }

    if (!token) {
        next("/login");
        return;
    }

    if (role === "admin" && !["/admin", "/profile"].includes(to.path)) {
        next("/admin");
        return;
    }

    if (role !== "admin" && to.path === "/admin") {
        next("/home");
        return;
    }

    if (to.meta.requiredRoles && Array.isArray(to.meta.requiredRoles)) {
        if (!to.meta.requiredRoles.includes(role)) {
            next(role === "admin" ? "/admin" : "/home");
            return;
        }
    }

    next();
});

export default router;