<template>
  <el-config-provider size="small">
    <div class="app-frame">
      <div :class="{ 'page-with-tabbar': !route.meta.hideTab }">
        <router-view />

        <AppTabbar v-if="!route.meta.hideTab" />
      </div>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from "element-plus";
import AppTabbar from "@/components/AppTabbar.vue";
import { useRoute } from "vue-router";

// 获取当前路由对象，用于判断 meta
const route = useRoute();
</script>