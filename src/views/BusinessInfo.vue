<template>
  <div class="page no-pad" v-loading="loading">
    <AppTopbar title="商家详情" :back="true" />

    <div class="biz-header" v-if="biz">
      <div class="biz-header-content">
        <el-image :src="fullImg(biz.img)" class="biz-logo" fit="cover">
          <template #error><el-icon><Picture /></el-icon></template>
        </el-image>
        <div class="biz-text-col">
          <h2 class="biz-title">{{ biz.name }}</h2>
          <div class="biz-tags">
            <span class="tag-item">配送约 {{ biz.eta || '30' }} 分钟</span>
            <span class="divider">|</span>
            <span class="tag-item">{{ biz.distance || '1.2km' }}</span>
          </div>
          <div class="biz-notice">公告：{{ biz.desc || '欢迎光临，本店为您提供最优质的服务。' }}</div>
        </div>
      </div>

      <div class="activity-row" v-if="promotionText">
        <span class="act-tag">满减</span>
        <span class="act-text">{{ promotionText }}</span>
      </div>
    </div>

    <div class="food-container">
      <div class="section-title">热销菜品</div>

      <el-empty v-if="!cart.items || cart.items.length === 0" description="商家暂未上架菜品" />

      <div v-for="f in cart.items" :key="f.id" class="food-item flat-list-item">
        <el-image :src="fullImg(f.img)" class="food-img" fit="cover">
          <template #error><el-icon><Picture /></el-icon></template>
        </el-image>

        <div class="food-detail">
          <div class="food-name">{{ f.name }}</div>
          <div class="food-desc">{{ f.desc || '暂无介绍' }}</div>

          <div class="food-sales">月售 {{ getStableSales(f.id) }} 单</div>

          <div class="food-action-row">
            <div class="food-price">￥{{ f.price }}</div>

            <div class="stepper">
              <template v-if="userStore.role !== 'business'">
                <div
                    v-if="f.quantity > 0"
                    class="op-btn minus"
                    @click="cart.setQuantity(f.id, f.quantity - 1)"
                >
                  <el-icon><Minus /></el-icon>
                </div>

                <span v-if="f.quantity > 0" class="qty-num">{{ f.quantity }}</span>

                <div
                    class="op-btn plus"
                    @click="cart.setQuantity(f.id, (f.quantity || 0) + 1)"
                >
                  <el-icon><Plus /></el-icon>
                </div>
              </template>
              <span v-else class="preview-text">商家预览</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="checkoutBar fixed-in-app">
      <template v-if="userStore.role !== 'business'">
        <div class="left" @click="toggleCartList">

          <div class="cart-icon-wrapper">
            <el-icon :size="28" class="cart-icon-svg"><ShoppingCart /></el-icon>

            <div class="my-red-dot" v-if="totalQty > 0">
              {{ totalQty > 99 ? '99+' : totalQty }}
            </div>
          </div>

          <div class="money-info">
            <div class="price-row">
              <span class="price" v-if="cart.totalPrice > 0">￥{{ cart.totalPrice.toFixed(2) }}</span>
              <span class="price empty" v-else>未选购商品</span>

              <span v-if="cart.discountAmount > 0" class="discount-tip">已减￥{{ cart.discountAmount }}</span>
            </div>
            <div class="tip">另需配送费 ￥{{ cart.deliveryFee }}</div>
          </div>
        </div>

        <div
            class="checkout-btn"
            :class="{ active: canCheckout }"
            @click="goConfirm"
        >
          {{ checkoutBtnText }}
        </div>
      </template>

      <template v-else>
        <div class="merchant-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>当前为商家预览模式</span>
        </div>
      </template>
    </div>

    <div style="height: 100px"></div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ShoppingCart, Picture, InfoFilled, Plus, Minus } from "@element-plus/icons-vue";
import AppTopbar from "../components/AppTopbar.vue";
import { useCartStore } from "../stores/cart";
import { useUserStore } from "@/stores/user";
import { fetchFoods, fetchBusinessInfo } from "@/api/business";
import { ElMessage } from "element-plus";


const router = useRouter();
const cart = useCartStore();
const userStore = useUserStore();
const loading = ref(true);
const route = useRoute();
const bizId = computed(() => Number(route.params.id));
const biz = ref({});


//计算购物车总数量
const totalQty = computed(() => {
  return cart.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

const promotionText = computed(() => {
  if (cart.promotions && cart.promotions.length > 0) {
    return cart.promotions.map(p => `满${p.limit}减${p.reduce}`).join('，');
  }
  return '';
});

const realFoodPrice = computed(() => {
  return cart.foodsTotal - cart.discountAmount;
});

const canCheckout = computed(() => {
  if (!biz.value) return false;
  if (totalQty.value <= 0) return false;
  return realFoodPrice.value >= biz.value.minOrder;
});

const checkoutBtnText = computed(() => {
  if (!biz.value) return '';
  if (totalQty.value <= 0) {
    return `￥${biz.value.minOrder} 起送`;
  }
  if (realFoodPrice.value < biz.value.minOrder) {
    const diff = biz.value.minOrder - realFoodPrice.value;
    return `还差 ￥${diff.toFixed(1)} 起送`;
  }
  return '去结算';
});

onMounted(async () => {
  try {
    const bizRes = await fetchBusinessInfo(bizId.value);
    biz.value = bizRes?.data || {};
    //用首页/列表带过来的距离与时间覆盖
    const qDistance = Array.isArray(route.query.distance) ? route.query.distance[0] : route.query.distance;
    const qEta = Array.isArray(route.query.eta) ? route.query.eta[0] : route.query.eta;
    if (qDistance) biz.value.distance = qDistance;
    if (qEta) biz.value.eta = qEta;

    // 前端模拟满减规则
    // 因为后端数据库没有 promotions 字段，这里手动给一个规则，让计算生效
    if (!biz.value.promotions) {
      biz.value.promotions = [
        { limit: 30, reduce: 5 },
        { limit: 60, reduce: 12 }
      ];
    }

    cart.setBusiness(biz.value);

    const foodRes = await fetchFoods(bizId.value);
    cart.setFoods(foodRes.data || []);
  } catch (e) {
    console.error("加载详情失败", e);
  } finally {
    loading.value = false;
  }
});

function goConfirm() {
  if (!canCheckout.value) {
    if (totalQty.value > 0) {
      ElMessage.warning(`未达到起送价，还差 ${ (biz.value.minOrder - realFoodPrice.value).toFixed(1) } 元`);
    }
    return;
  }
  router.push("/confirm-order");
}

function toggleCartList() {
  console.log("点击购物车");
}

function fullImg(path) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  if (path.startsWith('/img/')) return path;
  return `http://localhost:8080${path}`;
}

function getStableSales(id) {
  return (id * 12343 + 234) % 400 + 50;
}
</script>

<style scoped>
.page {
  padding: 0;
  background: #f5f5f5;
  min-height: 100vh;
}

.biz-header {
  background: linear-gradient(135deg, #0097ff 0%, #0076c5 100%);
  padding: 20px 16px 16px;
  color: #fff;
  position: relative;
  overflow: hidden;
}
.biz-header-content {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  z-index: 2;
  position: relative;
}
.biz-logo {
  width: 76px;
  height: 76px;
  border-radius: 6px;
  border: 1px solid rgba(255,255,255,0.4);
  flex-shrink: 0;
  background: #fff;
}
.biz-text-col {
  flex: 1;
  overflow: hidden;
  color: #fff;
}
.biz-title {
  margin: 0 0 6px 0;
  font-size: 19px;
  font-weight: bold;
  color: #fff;
}
.biz-tags {
  font-size: 12px;
  color: #fff;
  opacity: 0.95;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
}
.divider {
  margin: 0 6px;
  color: rgba(255,255,255,0.6);
}
.biz-notice {
  font-size: 12px;
  color: #fff;
  opacity: 0.9;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.activity-row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  z-index: 2;
  position: relative;
  color: #fff;
}
.act-tag {
  background: rgba(255,50,50,0.9);
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 10px;
  line-height: 1.2;
}
.act-text {
  color: #fff;
  font-weight: 500;
}

.food-container {
  background: #fff;
  border-radius: 12px 12px 0 0;
  margin-top: -12px;
  position: relative;
  z-index: 5;
  padding-bottom: 20px;
  min-height: 500px;
}
.section-title {
  padding: 16px;
  font-weight: bold;
  font-size: 16px;
  background: #fff;
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid #f5f5f5;
}
.food-item { display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #f9f9f9;
}
.food-img {
  width: 90px;
  height: 90px;
  border-radius: 6px;
  flex-shrink: 0;
  background: #f9f9f9;
  border: 1px solid #eee;
}
.food-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 90px;
  justify-content: space-between;
}
.food-name {
  font-weight: bold;
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}
.food-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.food-sales {
  font-size: 11px;
  color: #666;
  margin-bottom: 4px;
}
.food-action-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.food-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.op-btn {
  width: 22px; height: 22px; border-radius: 50%;
  display: flex; justify-content: center; align-items: center;
  cursor: pointer;
  transition: all 0.2s;
}
.op-btn.plus {
  background: #0097ff;
  color: #fff;
  border: 1px solid #0097ff;
}
.op-btn.plus:active {
  background: #0076c5;
}
.op-btn.minus {
  background: #fff;
  border: 1px solid #0097ff;
  color: #0097ff;
}
.op-btn.minus:active {
  background: #eef7ff;
}
.qty-num {
  font-size: 14px;
  font-weight: bold;
  min-width: 20px;
  text-align: center;
}

.checkoutBar {
  bottom: 0;
  background: #fff;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 999;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}
.left {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;
  padding-left: 80px;
  height: 100%;
  cursor: pointer;
}

/* 购物车图标容器 */
.cart-icon-wrapper {
  position: absolute;
  left: 16px; top: -15px;
  width: 50px; height: 50px;
  border-radius: 50%;
  background: #0097ff;
  border: 4px solid #fff;
  color: #fff;
  box-shadow: 0 -2px 5px rgba(0,0,0,0.1);
  z-index: 10;
  overflow: visible !important;
}

.cart-icon-svg {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
}

/* 红点样式 */
.my-red-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  background-color: #f56c6c;
  color: #fff;
  border: 2px solid #fff;
  border-radius: 10px;
  padding: 0 5px;
  min-width: 18px;
  height: 18px;
  font-size: 10px;
  font-weight: bold;
  line-height: 15px;
  text-align: center;
  z-index: 999;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.money-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.money-info .price {
  color: #333;
  font-size: 20px;
  font-weight: bold;
}
.money-info .price.empty {
  color: #999;
  font-size: 14px;
  font-weight: normal;
}
.discount-tip {
  font-size: 10px;
  color: #f56c6c;
  border: 1px solid #f56c6c;
  padding: 0 2px;
  border-radius: 2px;
}
.money-info .tip {
  font-size: 10px;
  color: #999;
}

.checkout-btn {
  width: 120px; height: 100%;
  background: #ddd; color: #999;
  font-weight: bold;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px;
  border: none;
  cursor: not-allowed;
  transition: all 0.3s;
}
.checkout-btn.active {
  background: #38ca73;
  color: #fff;
  cursor: pointer;
}
.merchant-tip {
  width: 100%;
  color: #e6a23c;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px; font-size: 14px;
}
</style>