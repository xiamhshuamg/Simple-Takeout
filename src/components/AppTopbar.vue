<template>
  <div class="topbar">
    <div class="navbar-content">
      <div
          class="left-icon"
          :class="{ 'is-clickable': back }"
          @click="onBack"
      >
        <el-icon v-if="back" size="22"><ArrowLeft /></el-icon>
      </div>

      <div class="title-text">{{ title }}</div>

      <div class="right-action">
        <slot name="right"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { ArrowLeft } from "@element-plus/icons-vue";

const props = defineProps({
  title: { type: String, default: "" },
  back: { type: Boolean, default: true },
});
const router = useRouter();

function onBack() {
  if (!props.back) return;
  router.back();
}
</script>

<style scoped>
.topbar {
  position:sticky;
  top: 0;
  z-index: 500;
  background: #17baf9;
  margin: 0 calc(var(--page-pad) * -1);
  padding-top: env(safe-area-inset-top);
}

.navbar-content {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  color: #fff;
}

.left-icon {
  width: 60px;
  height: 100%;
  display: flex;
  align-items: center;   /*垂直居中*/
  justify-content: flex-start;  /*水平靠左*/
  cursor: default;
}

.left-icon.is-clickable {
  cursor: pointer;
}

.title-text {
  font-size: 17px;
  font-weight: 600;
  flex: 1;/* flex: 1：占据剩余所有可用空间 */
  text-align: center;
  white-space: nowrap;  /* 文字不换行 */
  overflow: hidden;  /* 超出部分隐藏 */
}

.right-action {
  width: 60px; /* 与左侧保持一致宽度，确保标题居中 */
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 14px;
}
</style>