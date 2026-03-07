<template>
  <div class="page">
    <AppTopbar title="注册新账号" :back="true" />

    <div class="logo-area">
      <h1>加入饿了么</h1>
    </div>

    <el-card class="card" shadow="never">
      <el-tabs v-model="form.role" stretch class="role-tabs">
        <el-tab-pane label="我是普通用户" name="customer"></el-tab-pane>
        <el-tab-pane label="我是商家入驻" name="business"></el-tab-pane>
      </el-tabs>

      <el-form :model="form" :rules="rules" ref="regForm" label-width="0" style="margin-top: 20px">
        <el-form-item prop="account">
          <el-input v-model="form.account" :placeholder="accountPlaceholder" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="设置登录密码" show-password size="large">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="confirmPwd">
          <el-input v-model="form.confirmPwd" type="password" placeholder="确认登录密码" show-password size="large">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="doRegister" :loading="loading" class="submit-btn" size="large" round>
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="actions">
        <span class="link" @click="goLogin">已有账号？返回登录</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";
import AppTopbar from "@/components/AppTopbar.vue";
import { register as apiRegister } from "@/api/user";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const regForm = ref(null);
const loading = ref(false);

const form = reactive({
  role: "customer",
  account: "",
  password: "",
  confirmPwd: ""
});

const accountPlaceholder = computed(() => {
  if (form.role === 'customer') return "请输入手机号";
  return "请输入商家账号";
});

const rules = {
  account: [
    { required: true, message: "账号不能为空", trigger: "blur" },
    { pattern: /^\d+$/, message: "账号必须是纯数字", trigger: "blur" }
  ],
  password: [
    { required: true, message: "密码不能为空", trigger: "blur" },
    { min: 3, message: "密码至少3位", trigger: "blur" }
  ],
  confirmPwd: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: (rule, val, cb) => val !== form.password ? cb(new Error("两次密码不一致")) : cb(), trigger: 'blur' }
  ]
};

async function doRegister() {
  try {
    await regForm.value.validate();
  } catch (e) {
    return;
  }

  loading.value = true;
  try {
    const res = await apiRegister({
      role: form.role,
      account: form.account,
      password: form.password
    });

    console.log('注册返回:', res);

    if (res.code === 0 && res.data) {
      // 注册后自动登录
      userStore.setLoginInfo(res.data);

      // 延迟确保token存储完成
      await new Promise(resolve => setTimeout(resolve, 500));

      // 强制刷新用户信息
      await userStore.fetchUserInfo();

      ElMessage.success("注册成功");

      // 延迟跳转，确保用户信息已加载
      setTimeout(() => {
        router.replace("/home");
      }, 300);
    } else {
      ElMessage.error(res.msg || "注册失败");
    }
  } catch (e) {
    console.error('注册错误:', e);
    ElMessage.error(e.msg || "注册失败");
  } finally {
    loading.value = false;
  }
}

function goLogin() {
  router.push("/login");
}
</script>

<style scoped>
.page {
  padding: 20px;
  background: #fff;
  min-height: 100vh;
}
.logo-area {
  margin: 30px 0;
  text-align: center;
}
.logo-area h1 {
  color: #409eff;
  font-size: 26px;
}
.card {
  border: none;
  padding: 0;
}
.role-tabs {
  margin-bottom: 20px;
}
.submit-btn {
  width: 100%;
  font-weight: bold;
  font-size: 16px;
  margin-top: 10px;
}
.actions {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}
.link {
  color: #409eff;
  cursor: pointer;
  font-weight: bold;
}
</style>