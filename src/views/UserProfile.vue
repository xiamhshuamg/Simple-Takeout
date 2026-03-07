<template>
  <div class="page">
    <AppTopbar title="个人中心" :back="true" />

    <el-card class="profile-card" shadow="hover">
      <div class="user-info">
        <div class="avatar-wrapper" @click="handleAvatarClick">
          <el-avatar
              :shape="userStore.role === 'business' ? 'square' : 'circle'"
              :size="64"
              :src="fullAvatarUrl"
          />
          <el-icon class="camera-icon"><Camera /></el-icon>
        </div>

        <div class="text">
          <div class="name" @click="openEditName" style="cursor: pointer; display: flex; align-items: center; gap: 4px;">
            {{ displayName }}
            <el-icon :size="16" color="#999"><Edit /></el-icon>
          </div>

          <div class="role-tag">
            <el-tag size="small" effect="dark" v-if="userStore.role === 'customer'">普通用户</el-tag>
            <el-tag size="small" type="success" effect="dark" v-else-if="userStore.role === 'business'">商家用户</el-tag>
            <el-tag size="small" type="danger" effect="dark" v-else-if="userStore.role === 'admin'">管理员</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <div class="menu-list" v-if="userStore.role === 'customer'">
      <div class="menu-item" @click="router.push('/address-list')">
        <span>我的收货地址</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="router.push('/orders')">
        <span>我的订单</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item">
        <span>钱包余额</span>
        <span class="balance">￥{{ userStore.user?.balance || '99999' }}</span>
      </div>
    </div>

    <div class="menu-list" v-if="userStore.role === 'business'">
      <div class="section-title">店铺管理</div>

      <div class="menu-item">
        <div class="row-left">
          <el-icon :color="isOpen===1?'#67c23a':'#909399'"><SwitchButton /></el-icon>
          <span>营业状态</span>
        </div>
        <el-switch
            v-model="isOpen"
            :active-value="1"
            :inactive-value="0"
            active-text="营业中"
            inactive-text="休息中"
            inline-prompt
            @change="toggleBusinessStatus"
            :loading="statusLoading"
        />
      </div>

      <div class="menu-item" @click="router.push('/business/edit')">
        <div class="row-left">
          <el-icon color="#409eff"><Shop /></el-icon>
          <span>店铺信息设置</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="router.push('/business/food-manage')">
        <div class="row-left">
          <el-icon color="#e6a23c"><Food /></el-icon>
          <span>菜品管理</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="router.push('/business/order-handle')">
        <div class="row-left">
          <el-icon color="#67c23a"><List /></el-icon>
          <span>接单处理</span>
        </div>
        <el-tag type="danger" size="small" round>管理订单</el-tag>
      </div>

      <!-- 商家统计 -->
      <div v-if="userStore.role === 'business'" class="biz-stats">
        <div class="stat-item">
          <div class="stat-num">{{ stats.todayVisitors }}</div>
          <div class="stat-label">今日访客</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ stats.todayOrders }}</div>
          <div class="stat-label">今日订单</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">￥{{ stats.todayIncome }}</div>
          <div class="stat-label">今日收入</div>
        </div>
      </div>
    </div>
    <div class="menu-list" v-if="userStore.role === 'admin'">
      <div class="section-title">管理员操作</div>
      <div class="menu-item" @click="router.push('/admin')">
        <span>用户/商家管理</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="logout-area">
      <el-button type="danger" plain block size="large" @click="doLogout">退出当前账号</el-button>
    </div>

    <el-dialog v-model="showEditDialog" :title="userStore.role==='business'?'修改店铺名称':'修改昵称'" width="320px" center>
      <el-input v-model="editingName" placeholder="请输入新名称" clearable />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="saveName" :loading="savingName">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <div style="height: 60px"></div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, reactive, onUnmounted} from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import AppTopbar from "@/components/AppTopbar.vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { ArrowRight, Camera, Edit, Shop, Food, List, SwitchButton } from "@element-plus/icons-vue";
import { updateUserInfo } from "@/api/user";
import { http } from "@/api/http";

const router = useRouter();
const userStore = useUserStore();
const showEditDialog = ref(false);
const editingName = ref("");
const savingName = ref(false);

const isOpen = ref(0);
const statusLoading = ref(false);

// 统计数据响应式对象
const stats = reactive({
  todayOrders: 0,
  todayIncome: 0,
  todayVisitors: 0
});

const displayName = computed(() => {
  if (userStore.user) {
    return userStore.user.userName || userStore.user.name || userStore.user.businessName || "用户";
  }
  return "用户";
});

const fullAvatarUrl = computed(() => {
  if (!userStore.user?.avatar) {
    return userStore.role === 'business' ? '/img/img/sj01.png' : '/img/default.png';
  }
  const av = userStore.user.avatar; // 获取头像路径
  if (av.startsWith("http") || av.startsWith("/upload/")) {
    const timestamp = new Date().getTime();// 获取当前时间戳
    return av.startsWith("http")
        ? `${av}?t=${timestamp}`// 完整URL加时间戳参数
        : `http://localhost:8080${av}?t=${timestamp}`;// 本地路径拼接完
  }
  return av;
});

async function loadBusinessStatus() {
  try {
    const res = await http.get('/business/my-info');
    isOpen.value = (res.data.isOpen === 1) ? 1 : 0;
  } catch(e){}
}

// 加载统计数据
async function loadStats() {
  try {
    const res = await http.get('/business/stats');
    if(res.data) {
      stats.todayOrders = res.data.todayOrders || 0;
      stats.todayIncome = res.data.todayIncome || 0;
      stats.todayVisitors = res.data.todayVisitors || 0;
    }
  } catch(e) {
    console.error("加载统计失败", e);
  }
}

async function toggleBusinessStatus(val) {
  statusLoading.value = true;
  try {
    await http.post('/business/update-status', { isOpen: val });
    ElMessage.success(val === 1 ? "店铺已上线，将显示在首页" : "店铺已下线休息");
    if (userStore.user) {
      /* 更新本地用户信息中的营业状态 */
      userStore.user.isOpen = val;
    }
  } catch (e) {
    isOpen.value = val === 1 ? 0 : 1;
    ElMessage.error(e.msg || "状态修改失败");
  } finally {
    statusLoading.value = false;
  }
}
// 上传头像
async function handleAvatarClick() {
  if (!userStore.userId) {
    ElMessage.error("请先登录");
    return;
  }
  /* 创建文件输入元素，用于选择图片文件 */
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  /* 文件选择变化时的事件处理 */
  input.onchange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    try {
      let res;
      if (userStore.role === 'business') {
        res = await http.post('/upload/common', formData, {
          headers: {
            'Authorization': `Bearer ${userStore.token}`,
            'Content-Type': 'multipart/form-data'
          }
        });
        if (res.code === 0) {
          const newImageUrl = res.data;
          await http.post('/business/update-profile', { img: newImageUrl });
          userStore.updateUserField('avatar', newImageUrl);
          userStore.updateUserField('businessImg', newImageUrl);
          ElMessage.success('店铺图片更新成功');
          await userStore.fetchUserInfo();
        }
      } else {
        formData.append("userId", userStore.userId);
        res = await http.post('/upload/avatar', formData, {
          headers: {
            'Authorization': `Bearer ${userStore.token}`,
            'Content-Type': 'multipart/form-data'
          }
        });
        if (res.code === 0) {
          const newAvatarUrl = res.data;
          userStore.updateUserField('avatar', newAvatarUrl);
          await updateUserInfo({
            userName: userStore.user?.userName || userStore.user?.name || '用户',
            avatar: newAvatarUrl
          });
          ElMessage.success('头像更新成功');
          await userStore.fetchUserInfo();
        }
      }
    } catch (error) {
      console.error('上传失败:', error);
      ElMessage.error(error.response?.data?.msg || '上传失败');
    }
  };
  input.click();
}

function openEditName() {
  editingName.value = displayName.value;
  showEditDialog.value = true;
}

async function saveName() {
  if (!editingName.value.trim()) {
    ElMessage.warning("名称不能为空");
    return;
  }
  savingName.value = true;
  try {
    let res;
    if (userStore.role === 'business') {
      res = await http.post('/business/update-profile', {
        name: editingName.value }
      );
    } else {
      res = await updateUserInfo({
        userName: editingName.value,
        avatar: userStore.user?.avatar || ''
      });
    }

    if (res && res.code === 0) {
      if (userStore.role === 'business') {
        userStore.updateUserField('businessName', editingName.value);
        userStore.updateUserField('name', editingName.value);
      } else {
        userStore.updateUserField('userName', editingName.value);
        userStore.updateUserField('name', editingName.value);
      }
      ElMessage.success("修改成功");
      showEditDialog.value = false;
      await userStore.fetchUserInfo();
    } else {
      ElMessage.error(res?.msg || "修改失败");
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || "修改失败");
  } finally {
    savingName.value = false;
  }
}

function doLogout() {
  ElMessageBox.confirm("确定要退出登录吗?", "提示", {
    confirmButtonText: "退出",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    userStore.logout();
    router.replace("/login");
  }).catch(() => {});
}

onMounted(async () => {
  await userStore.fetchUserInfo();
  if (userStore.role === 'business') {
    loadBusinessStatus();
    loadStats(); // 调用加载统计
  }
});
</script>

<style scoped>
.page {
  padding: 12px;
}
.profile-card {
  border-radius: 12px;
  margin-bottom: 20px;
  background: #fff;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}
.name {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 5px;
}
.menu-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  padding-bottom: 8px;
}
.section-title {
  padding: 12px 16px 4px;
  font-size: 13px;
  color: #999;
  font-weight: bold;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  font-size: 15px;
  color: #333;
}

.menu-item:active {
  background: #fafafa;
}
.menu-item:last-child {
  border-bottom: none;
}
.balance {
  color: #f56c6c;
  font-weight: bold;
}
.logout-area {
  margin-top: 30px;
}
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  display: inline-block;
}
.camera-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  padding: 4px;
  font-size: 12px;
}
.row-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.biz-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0 15px;
  border-top: 1px solid #f5f5f5;
  margin-top: 10px;
}
.stat-item {
  text-align: center;
}
.stat-item .num {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}
.stat-item .label {
  font-size: 12px;
  color: #999;
}
</style>