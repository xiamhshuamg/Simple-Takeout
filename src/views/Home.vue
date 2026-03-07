<template>
  <div class="page">
    <div class="header-wrapper">
      <div class="header">
        <div class="location" @click="router.push('/location')">
          <el-icon size="20"><Location /></el-icon>
          <span class="addr-text">{{ locationStore.address }}</span>
          <el-icon size="16"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="searchBox">
        <el-input
            v-model="keyword"
            placeholder="搜索商家、商品名称"
            clearable
            class="custom-search-input"
            @clear="doSearch"
            @keyup.enter="doSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>

          <template #append>
            <el-button type="primary" @click="doSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="category-section">
      <div
          v-for="(item, index) in categories"
          :key="index"
          class="category-item"
          @click="goCategory(item)"
      >
        <el-image :src="item.img" class="category-icon" fit="contain" />
        <span class="category-name">{{ item.name }}</span>
      </div>
    </div>

    <div class="carouselWrap">
      <!-- 自动轮播，4秒切换一次，隐藏指示点，显示左右箭头 -->
      <el-carousel height="140px" :interval="4000" indicator-position="none" arrow="always">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <div class="banner" @click="goBanner(b)">
            <div class="banner-content">
              <div class="bannerTitle">{{ b.title }}</div>
              <div class="bannerSub">{{ b.sub }}</div>
              <el-button type="warning" round size="small">立即抢购</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <el-affix :offset="0">
      <div class="filterBar">
        <el-dropdown trigger="click" @command="handleSortCommand">
            <span class="filter-item" :class="{ active: ['default','price_asc','delivery_asc'].includes(sortType) }">
               {{ sortLabel }} <el-icon><ArrowDown /></el-icon>
            </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="default">综合排序</el-dropdown-item>
              <el-dropdown-item command="price_asc">起送价最低</el-dropdown-item>
              <el-dropdown-item command="delivery_asc">配送费最低</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <div class="filter-item" :class="{ active: sortType === 'distance' }" @click="setSort('distance')">
          距离最近
        </div>

        <div class="filter-item" :class="{ active: sortType === 'sales' }" @click="setSort('sales')">
          销量最高
        </div>

        <div class="filter-item" @click="showFilter = true">
          筛选 <el-icon><Filter /></el-icon>
        </div>
      </div>
    </el-affix>

    <el-drawer
        v-model="showFilter"
        title="更多筛选"
        direction="btt"
        size="auto"
        :with-header="true"
        class="mobile-filter-drawer"
    >
      <div class="drawer-content">
        <div class="drawerBlock">
          <div class="drawerTitle">商家服务</div>
          <div class="tags">
            <el-check-tag :checked="filterOpts.noDeliveryFee" @change="filterOpts.noDeliveryFee = !filterOpts.noDeliveryFee">免配送费</el-check-tag>
          </div>
        </div>
        <div class="drawerBlock">
          <div class="drawerTitle">评分区间</div>
          <el-radio-group v-model="filterOpts.minScore" size="small">
            <el-radio-button :label="0">全部</el-radio-button>
            <el-radio-button :label="4.0">4.0分以上</el-radio-button>
            <el-radio-button :label="4.5">4.5分以上</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="drawer-footer">
        <el-button style="flex:1" @click="resetFilter">重置</el-button>
        <el-button type="primary" style="flex:1" @click="showFilter = false">完成</el-button>
      </div>
    </el-drawer>

    <div class="section-header">推荐商家</div>

    <div class="bizList" v-loading="loading">
      <el-empty v-if="!loading && displayList.length === 0" description="暂无符合条件的商家" />

      <div
          v-for="b in displayList"
          :key="b.id"
          class="biz-item flat-list-item"
          @click="goBusiness(b)"
      >
        <div class="bizRow">
          <el-image :src="fullImg(b.img)" fit="cover" class="bizImg">
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>

          <div class="bizInfo">
            <div class="bizNameRow">
              <span class="bizName">{{ b.name }}</span>
              <el-tag size="small" type="warning" effect="dark" v-if="b.monthlySales > 500" class="hot-tag">热销</el-tag>
            </div>

            <div class="bizMeta">
              <div class="rate-box">
                <span class="score">{{ b.rating }}分</span>
                <span class="sales">月售 {{ b.monthlySales }}</span>
              </div>
              <div class="delivery-time">
                <span class="blue-text">{{ b.eta || '30分钟' }}</span>
                <span class="distance">{{ b.distance || '1.5km' }}</span>
              </div>
            </div>

            <div class="bizMeta">
              <div class="price-info">
                <span>起送 ￥{{ b.minOrder }}</span>
                <span class="divider">|</span>
                <span>配送 ￥{{ b.deliveryFee }}</span>
              </div>
            </div>

            <div class="bizTags" v-if="b.desc">
              <span class="mini-tag">{{ b.desc }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div style="height: 60px;"></div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowDown, Filter, Search, Location, ArrowRight, Picture } from "@element-plus/icons-vue";
import { useLocationStore } from "@/stores/location";
import { fetchBusinesses } from "@/api/business";

const locationStore = useLocationStore();
const router = useRouter();

// ============================
// 数据定义
// ============================
const keyword = ref("");
const loading = ref(false);
const rawList = ref([]);
const showFilter = ref(false);

const sortType = ref("default");
const sortLabel = ref("综合排序");

const filterOpts = reactive({
  minScore: 0,
  noDeliveryFee: false
});

// 分类数据
const categories = [
  { id: 1, name: '食物', img: 'img/img/dcfl01.png' },
  { id: 2, name: '早餐', img: 'img/img/dcfl02.png' },
  { id: 3, name: '跑腿代购', img: 'img/img/dcfl03.png' },
  { id: 4, name: '汉堡披萨', img: 'img/img/dcfl04.png' },
  { id: 5, name: '甜品饮品', img: 'img/img/dcfl05.png' },
  { id: 6, name: '速食简餐', img: 'img/img/dcfl06.png' },
  { id: 7, name: '地方小吃', img: 'img/img/dcfl07.png' },
  { id: 8, name: '米粉面馆', img: 'img/img/dcfl08.png' },
  { id: 9, name: '包子粥铺', img: 'img/img/dcfl09.png' },
  { id: 10, name: '炸鸡炸串', img: 'img/img/dcfl10.png' },
];

// 轮播图数据
const banners = [
  { id: 1, title: "品质套餐", sub: "搭配齐全吃得好", link: "/business-list" },
  { id: 2, title: "超级会员", sub: "每日享超值", link: "/business-list" },
  { id: 3, title: "限时折扣", sub: "爆款直降", link: "/business-list" },
];

onMounted(() => {
  loadData();
});

watch(() => [locationStore.latitude, locationStore.longitude], () => {
  loadData();
});

async function doSearch() {
  await loadData();
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      keyword: keyword.value,
      lat: locationStore.latitude,
      lng: locationStore.longitude
    };
    const res = await fetchBusinesses(params);
    rawList.value = res.data || [];
    if (keyword.value) {
      ElMessage.success(`找到 ${rawList.value.length} 家商家`);
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

const displayList = computed(() => {
  let list = [...rawList.value];
  if (filterOpts.minScore > 0) {
    list = list.filter(item => Number(item.rating) >= filterOpts.minScore);
  }
  if (filterOpts.noDeliveryFee) {
    list = list.filter(item => Number(item.deliveryFee) === 0);
  }

  if (sortType.value === 'distance') {
    list.sort((a, b) => parseDist(a.distance) - parseDist(b.distance));
  } else if (sortType.value === 'sales') {
    list.sort((a, b) => b.monthlySales - a.monthlySales);
  } else if (sortType.value === 'price_asc') {
    list.sort((a, b) => a.minOrder - b.minOrder);
  } else if (sortType.value === 'delivery_asc') {
    list.sort((a, b) => a.deliveryFee - b.deliveryFee);
  }
  return list;
});

function handleSortCommand(cmd) {
  sortType.value = cmd;
  /*根据选择的排序类型更新排序标签文字*/
  if (cmd === 'default') sortLabel.value = "综合排序";
  if (cmd === 'price_asc') sortLabel.value = "起送价最低";
  if (cmd === 'delivery_asc') sortLabel.value = "配送费最低";
}

function setSort(type) {
  if (sortType.value === type) {
    sortType.value = 'default';
  } else {
    sortType.value = type;
  }
}

function resetFilter() {
  filterOpts.minScore = 0;
  filterOpts.noDeliveryFee = false;
}

function parseDist(str) {
  if (!str) return 99999;
  return parseFloat(str.replace('km', ''));
}

function fullImg(path) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  if (path.startsWith('/img/')) return path;
  return `http://localhost:8080${path}`;
}

//点击分类，携带参数跳转到商家列表页
function goCategory(c) {
  // 跳转到 BusinessList 页面，并传递分类名称作为查询参数
  router.push({ path: "/business-list", query: { category: c.name } });
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


function goBanner(b) {
  router.push(b.link);
}
</script>

<style scoped>
.page {
  padding: 0;
  background: #f5f5f5;
  min-height: 100vh;
}

/*头部样式为纯蓝色背景 */
.header-wrapper {
  background-color: #0097ff;
  padding: 12px 16px;
  color: #fff;
}

.header {
  height: 40px;
  display: flex;
  align-items: center;
  font-weight: 600;
  margin-bottom: 8px;
}
/* 定位区域 */
.location {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  max-width: 100%;
}

.addr-text {
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.searchBox {
  width: 100%;
}

.custom-search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: none;
  background: #fff;
}

/* 分类宫格 */
.category-section {
  display: flex;
  flex-wrap: wrap;
  padding: 15px 5px;
  background: #fff;
  margin-bottom: 10px;
}

.category-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 15px;
  cursor: pointer;
  transition: transform 0.2s;
}

.category-item:active {
  transform: scale(0.95);
}

.category-icon {
  width: 45px;
  height: 45px;
  margin-bottom: 6px;
}

.category-name {
  font-size: 12px;
  color: #666;
}

/* 轮播图 */
.carouselWrap {
  margin: 10px 16px;
}

.banner {
  height: 100%;
  border-radius: 8px;
  background: url("/img/img/index_banner.png") center/cover no-repeat;
  padding: 16px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}

.bannerTitle {
  font-size: 20px;
  font-weight: 800;
  color: #333;
  margin-bottom: 4px;
}

.bannerSub {
  font-size: 14px;
  color: #555;
  margin-bottom: 10px;
}

/* 筛选栏 */
.filterBar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #fff;
  height: 44px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #666;
  width: 100%;
}

.filter-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-item.active {
  color: #0097ff;
  font-weight: bold;
}

/* 商家列表 */
.section-header {
  padding: 16px;
  background: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #f9f9f9;
  margin-top: 10px;
}

.bizList {
  background: #fff;
  display: flex;
  flex-direction: column;
}

.biz-item {
  cursor: pointer;
  padding: 20px 16px;
  display: flex;
  border-bottom: 1px solid #f9f9f9;
}

.bizRow {
  display: flex;
  gap: 16px;
  width: 100%;
}

.bizImg {
  width: 96px;
  height: 96px;
  border-radius: 6px;
  flex: 0 0 auto;
  background: #f5f5f5;
  border: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.bizInfo {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-top: 2px;
}

.bizNameRow {
  display: flex;
  align-items: center;
  gap: 6px;
}

.bizName {
  font-weight: 700;
  font-size: 17px;
  color: #333;
  line-height: 1.4;
}

.hot-tag {
  height: 20px;
  line-height: 18px;
  padding: 0 4px;
}

.bizMeta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
  margin-top: 6px;
}

.rate-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score {
  color: #ff9900;
  font-weight: bold;
  font-size: 15px;
}

.delivery-time {
  display: flex;
  gap: 10px;
}

.blue-text {
  color: #0097ff;
}

.price-info {
  color: #666;
  font-size: 13px;
}

.divider {
  margin: 0 6px;
  color: #eee;
}

.bizTags {
  margin-top: 8px;
  display: flex;
  gap: 4px;
}

.mini-tag {
  border: 1px solid #ff5722;
  color: #ff5722;
  font-size: 11px;
  padding: 1px 4px;
  border-radius: 2px;
}

/* 抽屉样式 */
.drawer-content {
  padding: 10px 20px;
}

.drawerBlock {
  margin-bottom: 20px;
}

.drawerTitle {
  font-weight: 700;
  margin-bottom: 10px;
  color: #333;
}

.drawer-footer {
  display: flex;
  gap: 10px;
  padding: 10px 20px 20px;
  border-top: 1px solid #eee;
}

:deep(.mobile-filter-drawer) {
  max-width: 100%;
}

@media (min-width: 768px) {
  :deep(.mobile-filter-drawer) {
    max-width: 500px;
    left: 50%;
    transform: translateX(-50%);
    margin: 0 auto;
  }
}
</style>