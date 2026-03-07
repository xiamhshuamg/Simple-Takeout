<template>
  <div class="page">
    <div class="top-bar">
      <el-icon @click="router.back()"><ArrowLeft /></el-icon>
      <div class="search-input">
        <el-icon><Search /></el-icon>
        <input
            v-model="keyword"
            id="tipinput"
            placeholder="小区/写字楼/学校"
            @input="onSearch"
        />
      </div>
    </div>

    <div class="current-section">
      <div class="label">当前定位</div>
      <div class="location-row" @click="reLocate">
        <el-icon class="icon"><Position /></el-icon>
        <span class="text">{{ currentStatus }}</span>
        <span class="re-btn" v-if="!loading">重新定位</span>
        <el-icon v-else class="is-loading"><Loading /></el-icon>
      </div>
    </div>

    <div class="list-section">
      <div class="label">附近地址</div>
      <div
          v-for="(item, index) in searchResults"
          :key="index"
          class="poi-item"
          @click="selectAddress(item)"
      >
        <div class="poi-name">{{ item.name }}</div>
        <div class="poi-addr">{{ item.district }}{{ item.address }}</div>
      </div>
    </div>
    <div id="i-am-hidden"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, Search, Position, Loading } from '@element-plus/icons-vue';
import { useLocationStore } from '@/stores/location';
import { ElMessage } from 'element-plus';

const router = useRouter();
const locationStore = useLocationStore();

const keyword = ref("");
const currentStatus = ref("正在定位...");
const loading = ref(true);
const searchResults = ref([]); // 存放搜索到的POI

// 高德地图对象
let mapObj = null;
let geolocation = null;
let placeSearch = null;

onMounted(() => {
  initMap();// 初始化地图
});

function initMap() {
  // 1. 初始化地图对象
  // 创建一个隐藏的 div 挂载地图
  mapObj = new AMap.Map('i-am-hidden', { resizeEnable: true });

  // 2. 加载插件
  AMap.plugin(['AMap.Geolocation', 'AMap.PlaceSearch'], function () {

    // 初始化定位插件
    geolocation = new AMap.Geolocation({
      enableHighAccuracy: true, // 使用高精度定位（GPS）
      timeout: 10000,           // 超过10秒后停止定位
    });

    // 初始化搜索插件
    placeSearch = new AMap.PlaceSearch({
      pageSize: 20, // 单页显示结果条数
      pageIndex: 1, // 页码
      extensions: 'all', // 返回详细信息
      type: '商务住宅|餐饮服务|生活服务|科教文化' // 搜索类别
    });

    // 页面一进来就开始定位
    getCurrentLocation();
  });
}

function getCurrentLocation() {
  loading.value = true;
  currentStatus.value = "正在获取位置...";

  geolocation.getCurrentPosition((status, result) => {
    loading.value = false;
    if (status === 'complete') {
      console.log('定位成功', result);

      //增加安全校验
      // 1. 确保 addressComponent 存在，如果不存在给一个空对象 {} 防止报错
      const component = result.addressComponent || {};

      // 2. 安全地获取地址名称
      // 优先取 建筑名 -> 小区名 -> 街道 -> 格式化全称 -> "未知位置"
      const simpleAddr = component.building ||
          component.neighborhood ||
          component.street ||
          result.formattedAddress ||
          "未知位置";

      // 3. 安全地获取城市
      const city = component.city || component.province || "昆明市";
      currentStatus.value = simpleAddr;

      // 调用 locationStore 的 setLocation 方法，保存地址信息
      locationStore.setLocation(
          simpleAddr,
          city,
          result.position.lat,
          result.position.lng
      );

      // 搜索周边
      searchNearBy(result.position.lng, result.position.lat);

    } else {
      console.error('定位失败', result);
      currentStatus.value = "定位失败，请手动选择";
      ElMessage.error("定位失败，请检查浏览器权限");
    }
  });
}

// 重新定位按钮
function reLocate() {
  getCurrentLocation();
}

// 根据经纬度搜索周边
function searchNearBy(lng, lat) {
  // 搜索中心点坐标周边 1000 米内的 POI
  placeSearch.searchNearBy('', [lng, lat], 1000, (status, result) => {
    if (status === 'complete' && result.info === 'OK') {
      searchResults.value = result.poiList.pois;
    }
  });
}

// 关键字搜索输入时触发
function onSearch() {
  if(!keyword.value) return;
  // 根据关键字在当前城市搜索
  placeSearch.search(keyword.value, (status, result) => {
    if (status === 'complete' && result.info === 'OK') {
      searchResults.value = result.poiList.pois;
    }
  });
}

// 用户点击列表某一项
function selectAddress(item) {
  locationStore.setLocation(
      item.name,
      item.pname + item.cityname, // 城市
      item.location.lat,
      item.location.lng
  );
  router.back(); // 返回首页
}
</script>

<style scoped>
.page {
  background: #fff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.top-bar {
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-bottom: 1px solid #f5f5f5;
}
.search-input {
  flex: 1;
  background: #f2f2f4;
  border-radius: 16px;
  display: flex;
  align-items: center;
  padding: 6px 12px;
  gap: 6px;
}
.search-input input {
  border: none;
  background: transparent;
  outline: none;
  flex: 1;
  font-size: 14px;
}
.current-section, .list-section {
  padding: 15px;
}
.label {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}
.location-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
  color: #333;
  cursor: pointer;
}
.icon {
  color: #409eff;
}
.re-btn {
  font-size: 12px;
  color: #409eff;
  margin-left: auto;
}

.poi-item {
  padding: 12px 0;
  border-bottom: 1px solid #f9f9f9;
  cursor: pointer;
}
.poi-name {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 4px;
}
.poi-addr {
  font-size: 12px;
  color: #666;
}
/*用于地图初始化的隐藏容器 */
#i-am-hidden {
  display: none;
}
</style>