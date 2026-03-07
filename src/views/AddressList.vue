<template>
  <div class="page">
    <AppTopbar title="我的收货地址" />

    <div class="list">
      <div v-for="item in list" :key="item.daId" class="addr-item" @click="selectAddr(item)">
        <div class="info">
          <div class="top">
            <span class="name">{{ item.contactName }}</span>
            <span class="sex">{{ item.contactSex === 1 ? '先生' : '女士' }}</span>
            <span class="tel">{{ item.contactTel }}</span>
          </div>
          <div class="detail">{{ item.address }}</div>
        </div>
        <!-- 操作区域：删除按钮 -->
        <div class="ops">
          <el-icon @click.stop="del(item.daId)"><Delete /></el-icon>
        </div>
      </div>
    </div>

    <el-button class="add-btn" type="primary" round @click="showAddDialog = true">新增地址</el-button>

    <el-dialog v-model="showAddDialog" title="新增地址" width="90%" class="mobile-dialog">
      <el-form :model="form">
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.contactSex">
            <el-radio :label="1">先生</el-radio>
            <el-radio :label="0">女士</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话"><el-input v-model="form.contactTel" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="doAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import AppTopbar from "@/components/AppTopbar.vue";
import { Delete } from "@element-plus/icons-vue";// 删除图标
import { listAddresses, addAddress, deleteAddress } from "@/api/address";
import { useRouter, useRoute } from 'vue-router';

const list = ref([]);
const showAddDialog = ref(false);
const router = useRouter();
const route = useRoute();

const form = reactive({
  contactName: '',
  contactSex: 1,
  contactTel: '',
  address: ''
});
const isSelectMode = route.query.select === 'true';// 判断当前是否是选择模式：通过路由查询参数select来判断

onMounted(load);

async function load() {
  const res = await listAddresses();// 调用API：listAddresses()获取地址列表
  list.value = res.data || []; // 将获取的数据赋给list.value，如果res.data不存在则使用空数组
}

async function doAdd() {
  await addAddress(form);
  showAddDialog.value = false;
  load();
}

async function del(id) {
  if(!confirm('确认删除？')) return;
  await deleteAddress(id);
  load();
}

function selectAddr(item) {
  if(isSelectMode) {
    localStorage.setItem('selectedAddr', JSON.stringify(item));
    router.back();
  }
}
</script>


<style scoped>
.page {
  padding: 12px;
  padding-bottom: 60px;
}
.addr-item {
  background: #fff;
  padding: 12px;
  margin-bottom: 10px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}
.top {
  font-weight: 700;
  margin-bottom: 4px;
}
.sex {
  font-weight: 400;
  font-size: 12px;
  margin: 0 8px;
  color: #666;
}
.detail {
  font-size: 13px;
  color: #555;
}
.add-btn {
  display: block;
  width: 80%;
  margin: 20px auto 0;
  height: 40px;
  font-size: 15px;
  font-weight: bold;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}
.ops{
  cursor: pointer;
}
</style>