<template>
  <div class="page">
    <AppTopbar title="我的订单" :back="true" />

    <el-tabs v-model="activeTab" @tab-change="loadOrders" class="order-tabs">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="待支付" name="unpaid"></el-tab-pane>
      <el-tab-pane label="已支付" name="paid"></el-tab-pane>
    </el-tabs>

    <el-empty v-if="orders.length === 0" description="暂无订单" />

    <el-collapse v-else accordion class="custom-collapse">
      <el-collapse-item
          v-for="o in orders"
          :key="o.id"
          :name="o.id"
      >
        <template #title>
          <div class="titleRow">
            <div class="shop">{{ o.businessName || '未知商家' }}</div>

            <div class="right">
              <span class="money">￥{{ Number(o.total).toFixed(2) }}</span>

              <el-tag v-if="o.status === 'paid'" type="success" size="small" effect="light">已支付</el-tag>

              <el-tag
                  v-else-if="isExpired(o.createdAt)"
                  type="info"
                  size="small"
                  effect="plain"
              >
                已超时
              </el-tag>

              <el-button
                  v-else
                  type="danger"
                  size="small"
                  round
                  @click.stop="goPay(o.id)"
              >
                去支付
              </el-button>
            </div>
          </div>
        </template>

        <div class="order-content">
          <div
              v-for="(it, idx) in o.items"
              :key="idx"
              class="line"
          >
            <span class="food-name">{{ it.name }} x {{ it.quantity }}</span>
            <span class="food-price">￥{{ (it.price * it.quantity).toFixed(0) }}</span>
          </div>

          <div class="line delivery-line">
            <span>配送费</span>
            <span>￥{{ o.deliveryFee }}</span>
          </div>

          <div class="meta">
            <div>下单时间：{{ formatTime(o.createdAt) }}</div>
            <div style="margin-top:4px">支付方式：{{ o.payMethod === 'wechat' ? '微信支付' : (o.payMethod === 'alipay' ? '支付宝' : '未支付') }}</div>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>

    <div style="height: 20px"></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppTopbar from "@/components/AppTopbar.vue";
import { fetchOrders } from "@/api/order";
import { useRouter } from "vue-router";

const router = useRouter();
const orders = ref([]);
const loading = ref(false);
const activeTab = ref("all");

async function loadOrders() {
  loading.value = true;
  try {
    const res = await fetchOrders();
    let list = res.data || [];
    if (activeTab.value !== 'all') {
      // 注意：这里过滤时，已超时的订单后端状态通常还是 'unpaid'
      // 如果你想在“待支付”标签页里隐藏已超时的订单，可以在这里加 isExpired 判断
      list = list.filter(o => o.status === activeTab.value);
    }
    // 按时间倒序
    list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    orders.value = list;
  } finally {
    loading.value = false;
  }
}

function goPay(orderId) {
  router.push(`/pay-online?orderId=${orderId}`);
}

// 【新增】判断是否超时 (15分钟)
function isExpired(createdAtStr) {
  if (!createdAtStr) return true;
  const createTime = new Date(createdAtStr).getTime();
  const now = Date.now();
  // 超过 15 分钟 (15 * 60 * 1000 毫秒) 即为超时
  return (now - createTime) > 15 * 60 * 1000;
}

function formatTime(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.page {
  padding: 12px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.order-tabs {
  margin-bottom: 10px;
  background: #fff;
  padding: 0 10px;
  border-radius: 8px;
}

.custom-collapse { border: none; background: transparent; }
:deep(.el-collapse-item__header) {
  height: auto !important;
  min-height: 54px;
  line-height: normal !important;
  padding: 12px !important;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
}
:deep(.el-collapse-item__wrap) {
  border-bottom: none;
  background: #fff;
  border-radius: 0 0 8px 8px;
  margin-bottom: 8px;
  margin-top: -8px;
}
.titleRow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}
.shop {
  font-weight: bold;
  font-size: 15px;
  color: #333;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 10px;
}
.right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 8px;
}
.money {
  font-weight: 900;
  font-size: 15px;
  color: #333;
}
.order-content {
  padding: 10px 12px;
}
.line {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
  color: #666;
}
.delivery-line {
  border-bottom: 1px dashed #eee;
  padding-bottom: 10px;
  margin-bottom: 10px;
}
.meta {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}
</style>