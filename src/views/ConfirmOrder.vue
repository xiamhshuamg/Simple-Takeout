<template>
  <div class="page bg-gray">
    <AppTopbar title="确认订单" />

    <div class="section-block addr-block" @click="goSelectAddress">
      <div class="addr-content" v-if="currentAddr">
        <div class="addr-top">
          <span class="addr-detail">{{ currentAddr.address }}</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="addr-user">{{ currentAddr.contactName }} {{ currentAddr.contactSex===1?'先生':'女士' }} {{ currentAddr.contactTel }}</div>
      </div>
      <div v-else class="no-addr">
        <span>请选择收货地址</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="section-block order-detail-block">
      <div class="block-header">{{ cart.business?.name || "（未选择商家）" }}</div>

      <div class="food-list-flat">
        <div v-for="it in orderFoods" :key="it.id" class="food-row-flat">
          <el-image :src="fullImg(it.img)" class="tiny-img" fit="cover" />
          <div class="name-col">{{ it.name }}</div>
          <div class="qty-col">x{{ it.quantity }}</div>
          <div class="price-col">￥{{ (it.price * it.quantity).toFixed(2) }}</div>
        </div>
      </div>

      <div class="fee-row-flat">
        <span>配送费</span>
        <span>￥{{ cart.deliveryFee }}</span>
      </div>
    </div>

    <div class="footerPay fixed-in-app">
      <div class="pay-left">
        <span>待支付 </span>
        <span class="total-money">￥{{ cart.totalPrice.toFixed(2) }}</span>
      </div>
      <el-button type="success" class="pay-btn" :disabled="orderFoods.length === 0" @click="goPay">
        去支付
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ArrowRight } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import AppTopbar from "@/components/AppTopbar.vue";
import { useCartStore } from "@/stores/cart";
import { listAddresses } from "@/api/address";

const router = useRouter();
const cart = useCartStore();
const currentAddr = ref(null);
const orderFoods = computed(() => {
  const arr = Array.isArray(cart.items) ? cart.items : [];
  return arr
      .filter(x => (x.quantity ?? x.qty ?? 0) > 0)
      .map(x => ({
        id: x.id ?? x.foodId,
        name: x.name ?? x.foodName ?? x.title ?? "",
        img: x.img ?? x.foodImg ?? x.image ?? "",
        price: Number(x.price ?? x.foodPrice ?? 0),
        quantity: x.quantity ?? x.qty ?? 0
      }));
});


onMounted(async () => {
  const selected = localStorage.getItem('selectedAddr');
  if (selected) {
    currentAddr.value = JSON.parse(selected);
    localStorage.removeItem('selectedAddr');
  } else {
    // 如果没有选中的地址，调用API获取地址列表
    const res = await listAddresses();
    const list = res.data || [];
    currentAddr.value = list.find(x => x.isDefault === 1) || list[0] || null;
  }
});

//图片路径补全函数
function fullImg(path) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  return `http://localhost:8080${path}`;
}

function goSelectAddress() {
    // 通过路由跳转到地址列表页，并传递select参数表示是选择模式
  router.push("/address-list?select=true");
}

async function goPay() {
  if (!currentAddr.value) return ElMessage.warning("请选择收货地址");
  try {
    const res = await cart.submitOrder(null, currentAddr.value.daId);
    router.push(`/pay-online?orderId=${res.orderId}`);
  } catch(e) {
    console.error(e);
  }
}
</script>

<style scoped>
.bg-gray { background: #f5f5f5; min-height: 100vh; padding-bottom: 60px; }

/* 区块通用样式 */
.section-block {
  background: #fff;
  margin-bottom: 10px;
  padding: 16px;
  border-top: 1px solid #f9f9f9;
  border-bottom: 1px solid #eee;
}

/* 地址块 */
.addr-block {
  margin-top: 10px;
  cursor: pointer;
}
.addr-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.addr-detail { font-size: 18px; font-weight: bold; color: #333; }
.addr-user { font-size: 14px; color: #666; }
.no-addr { display: flex; justify-content: space-between; align-items: center; color: #f56c6c; font-weight: bold; }

/* 订单详情 */
.block-header { font-weight: bold; padding-bottom: 12px; border-bottom: 1px solid #f5f5f5; margin-bottom: 10px; font-size: 16px; }

.food-row-flat { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px solid #f9f9f9; }
.food-row-flat:last-child { border-bottom: none; }
.tiny-img { width: 50px; height: 50px; border-radius: 4px; margin-right: 10px; background: #f5f5f5; }
.name-col { flex: 1; font-size: 14px; font-weight: 500; }
.qty-col { width: 40px; text-align: right; color: #999; font-size: 14px; }
.price-col { width: 70px; text-align: right; font-weight: bold; }

.fee-row-flat { display: flex; justify-content: space-between; padding: 12px 0; font-size: 14px; color: #333; }

/* 底部支付 */
.footerPay {
  bottom: 0;
  background: #333;
  color: #fff;
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 16px;
}
.pay-left {
  font-size: 14px;
}
.total-money {
  font-size: 20px;
  font-weight: bold;
  margin-left: 4px;
}
.pay-btn {
  height: 100%;
  border-radius: 0;
  width: 110px;
  font-size: 16px;
  font-weight: bold;
}
</style>