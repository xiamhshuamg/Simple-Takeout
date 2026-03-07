<template>
  <div class="page">
    <AppTopbar title="店铺设置" :back="true" />
    <div style="padding: 16px">
      <el-form :model="form" label-position="top">

        <el-form-item label="店铺封面">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:8080/upload/common"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleImgSuccess"
              :before-upload="beforeImgUpload"
          >
            <img v-if="form.img" :src="fullImgUrl(form.img)" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="店铺名称">
          <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="店铺公告 (用于搜索分类)">
          <el-input v-model="form.desc" type="textarea" rows="2" placeholder="例如：主营汉堡披萨、炸鸡炸串，美味实惠..." />
          <div class="tip-text">提示：把“汉堡披萨”、“甜品饮品”等分类词写在这里，首页点击分类就能跳转到了。</div>
        </el-form-item>

        <div class="row-2">
          <el-form-item label="经度 (Lng)">
            <el-input-number v-model="form.lng" :precision="6" :step="0.001" style="width: 100%" />
          </el-form-item>
          <el-form-item label="纬度 (Lat)">
            <el-input-number v-model="form.lat" :precision="6" :step="0.001" style="width: 100%" />
          </el-form-item>
        </div>
        <el-button type="success" plain size="small" @click="getBrowserLocation" style="width: 100%; margin-bottom: 20px;">
          <el-icon style="margin-right:4px"><Position /></el-icon> 获取我当前所在位置填入
        </el-button>

        <el-form-item label="满减优惠设置">
          <div v-for="(rule, index) in promotions" :key="index" class="rule-row">
            <span>满</span>
            <el-input-number v-model="rule.limit" :min="1" size="small" style="width: 80px" />
            <span>减</span>
            <el-input-number v-model="rule.reduce" :min="1" size="small" style="width: 80px" />
            <el-button type="danger" icon="Delete" circle size="small" @click="removeRule(index)" />
          </div>
          <el-button type="primary" plain size="small" icon="Plus" @click="addRule" style="width: 100%; margin-top: 8px;">
            添加满减规则
          </el-button>
        </el-form-item>

        <div class="row-2">
          <el-form-item label="起送价(元)">
            <el-input-number v-model="form.minOrder" :min="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="配送费(元)">
            <el-input-number v-model="form.deliveryFee" :min="0" style="width: 100%" />
          </el-form-item>
        </div>

        <el-button type="primary" block @click="save" :loading="loading" class="save-btn">保存修改</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { http } from "@/api/http";
import { ElMessage } from 'element-plus';
import AppTopbar from "@/components/AppTopbar.vue";
import { useUserStore } from "@/stores/user";
import { Plus, Delete, Position } from '@element-plus/icons-vue'; // 引入 Position 图标

const userStore = useUserStore();
const form = reactive({ name: '', desc: '', minOrder: 0, deliveryFee: 0, img: '', promotions: '', lat: null, lng: null });
const promotions = ref([]);
const loading = ref(false);
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}));

onMounted(async () => {
  try {
    const res = await http.get('/business/my-info');
    Object.assign(form, res.data);

    // 解析后端存的 promotions 字符串
    if (res.data.promotions) {
      try {
        promotions.value = JSON.parse(res.data.promotions);
      } catch(e) {
        promotions.value = [];
      }
    }
  } catch (e) {
    if (e.code !== 404) console.error(e);
  }
});

function fullImgUrl(path) {
  if (!path) return '';
  return path.startsWith('http') ? path : `http://localhost:8080${path}`;
}

function handleImgSuccess(response) {
  if (response.code === 0) {
    form.img = response.data;
    ElMessage.success("图片上传成功");
  } else {
    ElMessage.error(response.msg || "上传失败");
  }
}

function beforeImgUpload(rawFile) {
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
}

// 获取浏览器定位
function getBrowserLocation() {
  if (!navigator.geolocation) {
    ElMessage.error("您的浏览器不支持定位");
    return;
  }
  ElMessage.info("正在获取位置...");
  navigator.geolocation.getCurrentPosition(
      (position) => {
        form.lat = position.coords.latitude;
        form.lng = position.coords.longitude;
        ElMessage.success("获取成功！");
      },
      (err) => {
        console.error(err);
        ElMessage.error("获取位置失败，请手动输入");
      },
      { enableHighAccuracy: true, timeout: 5000 }
  );
}

function addRule() {
  promotions.value.push({ limit: 20, reduce: 5 });
}
function removeRule(index) {
  promotions.value.splice(index, 1);
}

async function save() {
  loading.value = true;
  try {
    form.promotions = JSON.stringify(promotions.value);
    await http.post('/business/update-info', form);
    ElMessage.success('保存成功');
    await userStore.fetchUserInfo();
  } catch(e) {
    ElMessage.error(e.msg || "保存失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.avatar-uploader .avatar { width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
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
  width: 100px;
  height: 100px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.row-2 {
  display: flex;
  gap: 16px;
}
.rule-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  background: #f9f9f9;
  padding: 6px;
  border-radius: 4px;
}
.save-btn {
  width: 100%;
  margin-top: 20px;
  height: 44px;
  font-weight: bold;
  border-radius: 22px;
}
.tip-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}
</style>