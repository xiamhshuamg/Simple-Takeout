<template>
  <div class="page bg-gray">
    <AppTopbar title="在线支付" />

    <div class="section-block">
      <div class="pay-header" :class="{ 'text-red': isTimeOut }">
        {{ isTimeOut ? "订单已超时" : `支付剩余时间 ${remainingTimeStr}` }}
      </div>

      <div class="pay-amount">￥{{ orderInfo.total ? Number(orderInfo.total).toFixed(2) : "0.00" }}</div>
      <div class="pay-shop">{{ orderInfo.businessName || "商家订单" }}</div>
    </div>

    <div class="section-block">
      <el-collapse v-model="collapseOpen" class="flat-collapse">
        <el-collapse-item name="1">
          <template #title>
            <span class="detail-title">订单详情</span>
          </template>

          <!--  明细 -->
          <template v-if="orderInfo.items && orderInfo.items.length > 0">
            <div v-for="(it, idx) in orderInfo.items" :key="idx" class="line">
              <span class="line-name">{{ it.name }} x {{ it.quantity }}</span>
              <span>￥{{ (Number(it.price) * Number(it.quantity)).toFixed(2) }}</span>
            </div>
          </template>

          <!--兜底提示 -->
          <div v-else class="empty-detail">暂无商品明细</div>

          <div class="line">
            <span>配送费</span>
            <span>￥{{ Number(orderInfo.deliveryFee || 0).toFixed(2) }}</span>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <div class="section-block">
      <div class="block-title">选择支付方式</div>
      <el-radio-group v-model="payMethod" class="pay-radio-group" :disabled="isTimeOut">
        <div class="pay-row" @click="!isTimeOut && (payMethod = 'alipay')">
          <div class="left">
            <img src="/img/img/alipay.png" class="payIcon" alt="支付宝" />
            <span>支付宝</span>
          </div>
          <el-radio label="alipay"><span></span></el-radio>
        </div>

        <div class="divider"></div>

        <div class="pay-row" @click="!isTimeOut && (payMethod = 'wechat')">
          <div class="left">
            <img src="/img/img/wechat.png" class="payIcon" alt="微信支付" />
            <span>微信支付</span>
          </div>
          <el-radio label="wechat"><span></span></el-radio>
        </div>
      </el-radio-group>
    </div>

    <div class="btn-area">
      <el-button
          type="success"
          class="confirm-btn"
          :loading="loading"
          :disabled="isTimeOut"
          @click="confirmPay"
      >
        {{
          isTimeOut
              ? "已超时，无法支付"
              : `确认支付 ￥${orderInfo.total ? Number(orderInfo.total).toFixed(2) : "0.00"}`
        }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import AppTopbar from "@/components/AppTopbar.vue";
import { useCartStore } from "@/stores/cart";
import { payOrder, getOrderDetail } from "@/api/order";

const route = useRoute();
const router = useRouter();
const cart = useCartStore();

const collapseOpen = ref(["1"]);
const payMethod = ref("alipay");
const loading = ref(false);
const isTimeOut = ref(false);

const remainingTimeStr = ref("计算中...");
let timer = null;

const orderIdStr = computed(() => {
  const raw = route.query.orderId;
  return Array.isArray(raw) ? raw[0] : raw;
});
const orderIdNum = computed(() => {
  const n = Number(orderIdStr.value);
  return Number.isFinite(n) ? n : null;
});

const orderInfo = ref({
  total: 0,
  deliveryFee: 66,
  businessName: "",
  items: [],
  createdAt: "",
  status: "",
});

/** 把各种可能字段统一成 items：[{name, quantity, price}] */
function normalizeOrderItems(data) {
  if (!data) return [];

  // 兼容不同字段名
  let candidate =
      data.items ||
      data.orderItems ||
      data.orderDetailList ||
      data.details ||
      data.foods;

  // 兼容 items 可能是 JSON 字符串
  if (typeof candidate === "string") {
    try {
      candidate = JSON.parse(candidate);
    } catch {
      candidate = null;
    }
  }

  if (!Array.isArray(candidate)) return [];

  return candidate
      .map((x) => ({
        name: x.name || x.foodName || x.title || "",
        quantity: Number(x.quantity ?? x.qty ?? x.count ?? 0),
        price: Number(x.price ?? x.foodPrice ?? 0),
      }))
      .filter((x) => x.name && x.quantity > 0);
}

function fallbackItemsFromCart() {
  const arr = Array.isArray(cart.items) ? cart.items : [];
  return arr
      .filter((it) => Number(it.quantity || 0) > 0)
      .map((it) => ({
        name: it.name || it.foodName || "",
        quantity: Number(it.quantity || 0),
        price: Number(it.price || 0),
      }))
      .filter((x) => x.name && x.quantity > 0);
}

function startCountDown(createdAtStr) {
  if (!createdAtStr) return;
  const createTime = new Date(createdAtStr).getTime();
  if (!Number.isFinite(createTime)) return;

  const expireTime = createTime + 15 * 60 * 1000;

  const tick = () => {
    const now = Date.now();
    const diff = expireTime - now;

    if (diff <= 0) {
      remainingTimeStr.value = "00:00";
      isTimeOut.value = true;
      if (timer) clearInterval(timer);
      return;
    }

    const m = Math.floor(diff / 60000);
    const s = Math.floor((diff % 60000) / 1000);
    remainingTimeStr.value = `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  };

  tick();
  timer = setInterval(tick, 1000);
}

onMounted(async () => {
  if (!orderIdNum.value) {
    ElMessage.error("订单号缺失");
    return;
  }

  try {
    const res = await getOrderDetail(orderIdNum.value);
    const data = res?.data || {};

    // 基础字段（避免整包覆盖导致 items 丢失）
    orderInfo.value.total = Number(data.total ?? data.payAmount ?? orderInfo.value.total ?? 0);
    orderInfo.value.deliveryFee = Number(data.deliveryFee ?? orderInfo.value.deliveryFee ?? 0);
    orderInfo.value.businessName = data.businessName || data.shopName || cart.business?.name || "";
    orderInfo.value.createdAt = data.createdAt || data.createTime || "";
    orderInfo.value.status = data.status || "";

    //订单已支付 -> 跳转
    if (orderInfo.value.status === "paid") {
      ElMessage.success("订单已支付");
      router.replace("/orders");
      return;
    }

    //items 归一化 + 兜底
    const normalized = normalizeOrderItems(data);
    orderInfo.value.items = normalized.length > 0 ? normalized : fallbackItemsFromCart();

    startCountDown(orderInfo.value.createdAt);
  } catch (e) {
    console.error(e);
    ElMessage.error("获取订单信息失败");
  }
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

async function confirmPay() {
  if (isTimeOut.value) {
    ElMessage.warning("订单已超时，无法支付");
    return;
  }
  if (!orderIdNum.value) return;

  try {
    loading.value = true;
    await payOrder({
      orderId: orderIdNum.value,
      payMethod: payMethod.value,
    });
    ElMessage.success("支付成功");
    cart.clearCart();
    router.push("/orders");
  } catch (error) {
    ElMessage.error(error?.message || "支付失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.bg-gray {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.section-block {
  background: #fff;
  margin-bottom: 10px;
  padding: 20px;
}

.pay-header {
  text-align: center;
  color: #666;
  font-size: 13px;
  margin-bottom: 10px;
}

.text-red {
  color: #f56c6c;
  font-weight: bold;
}

.pay-amount {
  text-align: center;
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 6px;
}

.pay-shop {
  text-align: center;
  font-size: 14px;
  color: #999;
}

.flat-collapse :deep(.el-collapse-item__header) {
  border: none;
  font-size: 14px;
  color: #666;
}
.flat-collapse :deep(.el-collapse-item__wrap) {
  border: none;
}

.detail-title {
  color: #333;
  font-weight: 500;
}

.line {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
  color: #666;
}
.line-name {
  max-width: 70%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-detail {
  padding: 10px 0;
  color: #999;
  font-size: 13px;
}

.block-title {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 16px;
}

.pay-radio-group {
  width: 100%;
  display: block;
}

.pay-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  cursor: pointer;
}

.pay-row .left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
}

.payIcon {
  width: 24px;
  height: 24px;
}

.divider {
  height: 1px;
  background: #f5f5f5;
  margin-left: 34px;
}

.btn-area {
  padding: 20px;
}

.confirm-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 4px;
}
</style>
