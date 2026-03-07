<template>
  <div class="page">
    <AppTopbar title="管理员后台" :back="false">
      <template #right>
        <div class="logout-btn" @click="doLogout">
          <el-icon style="margin-right:2px; top:1px;"><SwitchButton /></el-icon>
          退出
        </div>
      </template>
    </AppTopbar>

    <el-tabs v-model="tab" class="tabs">
      <el-tab-pane label="用户管理" name="customers">
        <div class="actions">
          <el-button size="small" icon="Refresh" @click="loadCustomers">刷新</el-button>
        </div>

        <el-table
            :data="customers"
            v-loading="loadingCustomers"
            row-key="id"
            style="width: 100%"
            stripe
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="account" label="账号" width="100"/>
          <el-table-column prop="name" label="昵称" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="onDeleteCustomer(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="商家管理" name="businesses">
        <div class="actions">
          <el-button size="small" icon="Refresh" @click="loadBusinesses">刷新</el-button>
        </div>

        <el-table
            :data="businesses"
            v-loading="loadingBusinesses"
            row-key="id"
            style="width: 100%"
            stripe
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="店铺名" />
          <el-table-column prop="isOpen" label="状态" width="70">
            <template #default="{ row }">
              <el-tag :type="row.isOpen === 1 ? 'success' : 'info'" size="small">
                {{ row.isOpen === 1 ? "营业" : "打烊" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="onDeleteBusiness(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh, SwitchButton } from "@element-plus/icons-vue";
import AppTopbar from "@/components/AppTopbar.vue";
import { listCustomers, listBusinesses, deleteCustomer, deleteBusiness } from "@/api/admin";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();

// 当前 tab
const tab = ref("customers");

// 列表数据
const customers = ref([]);
const businesses = ref([]);
const loadingCustomers = ref(false);
const loadingBusinesses = ref(false);

// 格式化数据
function normalizeCustomer(x) {
  return {
    id: x.customerId ?? x.id,
    account: x.customerAccount ?? x.account,
    name: x.userName ?? x.name ?? "用户",
  };
}
function normalizeBusiness(x) {
  return {
    id: x.businessId ?? x.id,
    account: x.businessAccount ?? x.account,
    name: x.businessName ?? x.name ?? "商家",
    isOpen: x.isOpen ?? 0,
  };
}

// 加载数据
async function loadCustomers() {
  loadingCustomers.value = true;
  try {
    const res = await listCustomers();
    const arr = res.data || [];
    customers.value = Array.isArray(arr) ? arr.map(normalizeCustomer) : [];
  } catch (e) {
    ElMessage.error(e?.msg || "加载用户失败");
  } finally {
    loadingCustomers.value = false;
  }
}

async function loadBusinesses() {
  loadingBusinesses.value = true;
  try {
    const res = await listBusinesses();
    const arr = res.data || [];
    businesses.value = Array.isArray(arr) ? arr.map(normalizeBusiness) : [];
  } catch (e) {
    ElMessage.error(e?.msg || "加载商家失败");
  } finally {
    loadingBusinesses.value = false;
  }
}

// 删除逻辑
async function onDeleteCustomer(row) {
  try {
    await ElMessageBox.confirm(
        `确定删除用户【${row.name}】吗？`,
        "警告",
        { type: "warning", confirmButtonText: "删除", cancelButtonText: "取消" }
    );
    await deleteCustomer(row.id);
    ElMessage.success("删除成功");
    await loadCustomers();
  } catch (e) {}
}

async function onDeleteBusiness(row) {
  try {
    await ElMessageBox.confirm(
        `确定删除商家【${row.name}】吗？`,
        "警告",
        { type: "warning", confirmButtonText: "删除", cancelButtonText: "取消" }
    );
    await deleteBusiness(row.id);
    ElMessage.success("删除成功");
    await loadBusinesses();
  } catch (e) {}
}

// 退出登录逻辑
function doLogout() {
  ElMessageBox.confirm("确定要退出管理员登录吗?", "提示", {
    confirmButtonText: "退出",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    userStore.logout();
    router.replace("/login");
    ElMessage.success("已退出登录");
  }).catch(() => {});
}

onMounted(async () => {
  await loadCustomers();
  await loadBusinesses();
});
</script>

<style scoped>
.page {
  padding: 12px;
}
.tabs {
  margin-top: 10px;
  background: #fff;
  border-radius: 12px;
  padding: 8px;
}
.actions {
  display: flex;
  justify-content: flex-end;
  padding: 6px 0 10px;
}
.logout-btn {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-weight: bold;
}
.logout-btn:active {
  opacity: 0.7;
}
</style>