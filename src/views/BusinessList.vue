<template>
  <div class="page">
    <AppTopbar title="全部商家" />

    <el-alert
        v-if="category"
        :title="`当前分类：${category}`"
        type="info"
        show-icon
        :closable="false"
        style="margin: 0; border-radius: 0;"
    />

    <div class="list" v-loading="loading">
      <el-empty v-if="!loading && businesses.length === 0" description="暂无商家" />

      <div
          v-for="b in businesses"
          :key="b.id"
          class="flat-list-item business-item"
          @click="goBusiness(b)"
      >
        <div class="row">
          <el-image :src="fullImg(b.img)" class="img" fit="cover">
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>

          <div class="info">
            <div class="name">{{ b.name }}</div>

            <div class="row-between">
              <div class="meta">
                <span class="score">{{ b.rating }}分</span>
                <span style="margin-left: 8px">月售 {{ b.monthlySales }}</span>
              </div>
              <div class="meta">
                <span style="color: #0097ff">{{ b.eta||30 }}</span>
                <span style="margin-left: 5px">{{ b.distance || '1.2km' }}</span>
              </div>
            </div>

            <div class="meta">
              <span>起送 ￥{{ b.minOrder }}</span>
              <span class="divider">|</span>
              <span>配送 ￥{{ b.deliveryFee }}</span>
            </div>

            <div class="desc" v-if="b.desc">{{ b.desc }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import AppTopbar from "@/components/AppTopbar.vue";
import { Picture } from "@element-plus/icons-vue";
import { fetchBusinesses } from "@/api/business";
import { useLocationStore } from "@/stores/location";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const businesses = ref([]);
const locationStore = useLocationStore();

const category = computed(() => route.query.category || "");

onMounted(() => {
  loadData();
});

async function loadData() {
  loading.value = true;
  try {
    // 调用后端接口
    const params = {
      keyword: category.value,
      lat: locationStore.latitude, // 传入经度
      lng: locationStore.longitude // 传入纬度
    };
    if (category.value) {

      params.keyword = category.value;
    }
    const res = await fetchBusinesses(params);
    businesses.value = res.data || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function goBusiness(b) {
  router.push({
    path: `/business/${b.id}`,
    query: {
      eta: b?.eta ?? "",
      distance: b?.distance ?? ""
    }
  });
}



// 图片路径修复函数
function fullImg(path) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  if (path.startsWith('/img/')) return path;
  return `http://localhost:8080${path}`;
}
</script>

<style scoped>
/* 样式重写 */
.page {
  background: #fff;
  min-height: 100vh;
}
.list {
  display: flex;
  flex-direction: column;
}
.business-item {
  cursor: pointer;
}
.row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.img {
  width: 76px;
  height: 76px;
  border-radius: 4px;
  background: #f5f5f5;
  border: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  flex-shrink: 0;
}
.info { flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 76px;
}
.info .name {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 4px;
  color: #333;
}
.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
  display: flex;
  align-items: center;
}
.score {
  color: #ff9900;
  font-weight: bold;
}
.divider {
  margin: 0 6px;
  color: #ddd;
}
.desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>