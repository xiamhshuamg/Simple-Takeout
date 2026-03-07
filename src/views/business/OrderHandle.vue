<template>
  <div class="page">
    <AppTopbar title="订单处理" :back="true" />

    <el-empty v-if="orders.length === 0" description="暂无订单" />

    <div class="order-list" v-else>
      <el-card v-for="o in orders" :key="o.id" shadow="never" class="order-card">
        <template #header>
          <div class="card-header">
            <span class="order-id">订单号：{{ o.id }}</span>
            <el-tag :type="o.status==='paid'?'success':'warning'" size="small">
              {{ o.status==='paid'?'已支付':'未支付' }}
            </el-tag>
          </div>
        </template>

        <div class="customer-info">
          <div class="row">
            <el-icon><User /></el-icon> {{ o.receiverName }}
            <span style="margin:0 8px">|</span>
            <el-icon><Phone /></el-icon> {{ o.receiverPhone }}
          </div>
          <div class="addr-row">
            <el-icon style="margin-top:2px"><LocationInformation /></el-icon>
            <span>{{ o.receiverAddress }}</span>
          </div>
        </div>

        <div class="divider"></div>

        <div class="food-list">
          <div v-for="(it, idx) in o.items" :key="idx" class="food-row">
            <span class="fname">{{ it.name }}</span>
            <span class="fqty">x{{ it.quantity }}</span>
            <span class="fprice">￥{{ it.price }}</span>
          </div>
        </div>

        <div class="footer">
          <div class="time">{{ o.createdAt }}</div>
          <div class="total">
            实入账：<span class="money">￥{{ o.total }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <div style="height: 20px"></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { http } from "@/api/http";
import AppTopbar from "@/components/AppTopbar.vue";
import { User, Phone, LocationInformation } from '@element-plus/icons-vue';

const orders = ref([]);

onMounted(load);

async function load() {
  try {
    const res = await http.get('/order/merchant-list');
    orders.value = res.data || [];
  } catch (e) {
    console.error(e);
  }
}
</script>

<style scoped>
.page {
  padding: 12px;
}
.order-card {
  margin-bottom: 12px;
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  font-size: 13px;
  color: #666;
}
.customer-info {
  padding: 10px 0;
  font-size: 14px;
  font-weight: bold;
  color: #333;
}
.row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}
.addr-row {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  color: #666;
  font-size: 13px;
  font-weight: normal;
}
.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 5px 0;
}
.food-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
}
.fname {
  flex: 1;
}
.fqty {
  width: 40px;
  text-align: right;
  color: #999;
}
.fprice {
  width: 60px;
  text-align: right;
  font-weight: bold;
}
.footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  font-size: 12px;
  color: #999;
}
.money {
  color: #f56c6c;
  font-weight: 900;
  font-size: 16px;
}
</style>