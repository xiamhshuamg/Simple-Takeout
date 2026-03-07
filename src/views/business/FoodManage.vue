<template>
  <div class="page">
    <AppTopbar title="菜品管理" :back="true" />

    <div class="list">
      <el-empty v-if="foods.length === 0" description="暂无菜品" />

      <div v-for="item in foods" :key="item.id" class="food-item">
        <el-image
            :src="fullImg(item.img)"
            :style="{filter: item.status===0?'grayscale(100%)':'none'}"
            style="width:60px; height:60px; border-radius:4px"
            fit="cover"
        />
        <div class="info">
          <div class="name">
            {{ item.name }}
            <el-tag v-if="item.status===0" type="info" size="small" style="margin-left:4px">已下架</el-tag>
          </div>
          <div class="price">￥{{ item.price }}</div>
        </div>

        <el-switch
            v-model="item.status"
            :active-value="1"
            :inactive-value="0"
            size="small"
            style="margin-right: 12px"
            @change="(val) => toggleFood(item, val)"
        />

        <el-button type="danger" size="small" icon="Delete" circle @click="del(item.id)"></el-button>
      </div>
    </div>

    <el-button type="primary" round class="add-btn" @click="showAdd=true">
      <el-icon style="margin-right:4px"><Plus /></el-icon> 添加新菜品
    </el-button>

    <el-dialog v-model="showAdd" title="添加菜品" width="90%" class="mobile-dialog">
      <el-form :model="newFood" label-position="top">
        <el-form-item label="菜名"><el-input v-model="newFood.name"/></el-form-item>
        <el-form-item label="描述"><el-input v-model="newFood.desc"/></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="newFood.price" :min="0"/></el-form-item>
        <el-form-item label="菜品图片">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:8080/upload/common"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="(res) => { if(res.code===0) newFood.img = res.data }"
          >
            <img v-if="newFood.img" :src="fullImg(newFood.img)" style="width:80px;height:80px;border-radius:4px" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd=false">取消</el-button>
        <el-button type="primary" @click="doAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { http } from "@/api/http";
import { useUserStore } from "@/stores/user";
import AppTopbar from "@/components/AppTopbar.vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete, Plus } from '@element-plus/icons-vue';

const userStore = useUserStore();
const foods = ref([]);
const showAdd = ref(false);
const newFood = reactive({ name: '', desc: '', price: 10, img: '' });
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}));

onMounted(load);

async function load() {
  const res = await http.get(`/business/${userStore.userId}/foods-all`);
  foods.value = res.data || [];
}

//图片路径补全函数
function fullImg(path) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  return `http://localhost:8080${path}`;
}

async function doAdd() {
  if(!newFood.img) newFood.img = '/img/img/sp01.png';
  await http.post('/business/food/save', newFood);
  ElMessage.success('添加成功');
  showAdd.value = false;
  newFood.name = ''; newFood.desc=''; newFood.price=10; newFood.img='';
  load();
}

async function del(id) {
  try {
    await ElMessageBox.confirm('确定删除该菜品吗？');
    await http.delete(`/business/food/${id}`);
    ElMessage.success('已删除');
    load();
  } catch(e) {}
}

async function toggleFood(item, val) {
  try {
    await http.post('/business/food/status', {
      foodId: item.id,
      status: val
    });
    ElMessage.success(val === 1 ? "已上架" : "已下架");
  } catch(e) {
    item.status = val === 1 ? 0 : 1;
    ElMessage.error("操作失败");
  }
}
</script>

<style>
.mobile-dialog.el-dialog {
  width: 90% !important;
  max-width: 380px !important;
  margin-top: 20vh !important;
  border-radius: 12px !important;
}
</style>

<style scoped>
.page {
  padding: 12px;
  padding-bottom: 80px;
}
.add-btn {
  display: block;
  width: 90%;
  margin: 20px auto 0;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 22px;
  box-shadow: 0 4px 10px rgba(64,158,255,0.3);
}
.food-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.02);
}
.info {
  flex: 1;
}
.name {
  font-weight: bold;
  display: flex;
  align-items: center
}
.price {
  color: #f56c6c;
  margin-top: 4px;
  font-weight: bold;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 80px;
  height: 80px;
  text-align: center;
  display:flex;
  align-items:center;
  justify-content:center;
}
</style>