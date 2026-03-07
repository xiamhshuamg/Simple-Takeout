<template>
  <div v-if="showTabbar" class="tabbar-wrapper">
    <div class="tabbar">
      <div class="tab-item" :class="{ active: active === '/home' }" @click="goTo('/home')">
        <el-icon :size="22"><House /></el-icon>
        <span>首页</span>
      </div>

      <div class="tab-item" :class="{ active: active === '/business-list' }" @click="goTo('/business-list')">
        <el-icon :size="22"><Compass /></el-icon>
        <span>发现</span>
      </div>

      <div
          v-if="showOrdersTab"
          class="tab-item"
          :class="{ active: active === '/orders' }"
          @click="goTo('/orders')"
      >
        <el-icon :size="22"><Tickets /></el-icon>
        <span>订单</span>
      </div>

      <div class="tab-item" :class="{ active: active === '/profile' }" @click="goTo('/profile')">
        <el-icon :size="22"><User /></el-icon>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { House, Compass, Tickets, User } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 判断高亮：只要当前路由以 /home 开头就高亮首页，以此类推
const active = computed(() => {
  // 简单匹配，如果路径层级较深可根据需求改为 includes
  return route.path;
});

const showTabbar = computed(() => userStore.role !== "admin");
const showOrdersTab = computed(() => userStore.role === "customer");

function goTo(path) {
  if (route.path === path) return;
  router.push(path);
}
</script>

<style scoped>
/* 让导航栏在PC端也居中且宽度受限 */
.tabbar-wrapper {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 768px;
  z-index: 999;
  background: #fff;
  border-top: 1px solid #eee;
}

.tabbar {
  display: flex;
  height: 56px;
  align-items: center;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #999;
  gap: 4px; /* 图标和文字的间距 */
  cursor: pointer;
}

.tab-item.active {
  color: #0097ff;
  font-weight: bold;
}
</style>






