<template>
  <div class="page">
    <div class="login-container">
      <div class="logo">
        <el-icon :size="60" color="#409eff"><Eleme /></el-icon>
        <h2>饿了么</h2>
      </div>

      <el-card class="login-card" shadow="never">
        <el-form :model="form" :rules="rules" ref="loginForm" label-width="0">
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号 / 商家号" size="large">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
                size="large"
                @keyup.enter="doLogin"
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="doLogin" :loading="loading" class="login-btn" size="large" round>
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="footer-actions">
          <span class="text">还没有账号？</span>
          <span class="link" @click="goRegister">立即注册</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Eleme } from "@element-plus/icons-vue";
import { login as apiLogin } from "@/api/user";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const loginForm = ref(null);
const loading = ref(false);

const form = reactive({ phone: "", password: "" });

const rules = {
  phone: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

async function doLogin() {
  try {
    await loginForm.value.validate();//表单检查
  } catch (e) {
    return;
  }

  loading.value = true;

  try {
    const res = await apiLogin(form);//账号和密码打包发给后端接口

    if (res.code === 0 && res.data) {
      // 保存登录信息
      userStore.setLoginInfo(res.data);

      // 延迟确保token存储完成
      await new Promise(resolve => setTimeout(resolve, 500));

      // 刷新用户信息
      await userStore.fetchUserInfo();

      ElMessage.success(`登录成功，欢迎 ${res.data.name}`);

      // 延迟跳转，确保用户信息已加载
      setTimeout(() => {
        router.replace(res.data.role === "admin" ? "/admin" : "/home");
      }, 300);
    } else {
      ElMessage.error(res.msg || "登录失败");
    }
  } catch (e) {
    console.error("登录错误:", e);
    ElMessage.error(e.msg || e.response?.data?.msg || "登录失败");
  } finally {
    loading.value = false;
  }
}

function goRegister() {
  router.push("/register");
}
</script>

<style scoped>
.page {
  background: linear-gradient(180deg, #e0f2ff 0%, #fff 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-container {
  width: 100%;
  max-width: 400px;
}
.logo {
  text-align: center;
  margin-bottom: 30px;
}
.logo h2 {
  margin: 10px 0 0;
  color: #409eff;
  font-family: sans-serif;
  letter-spacing: 2px;
}
.login-card {
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(64,158,255,0.1);
  border: none;
}
.login-btn {
  width: 100%;
  font-weight: bold;
  font-size: 16px;
  margin-top: 10px;
}
.footer-actions {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}
.text {
  color: #999;
}
.link {
  color: #409eff;
  cursor: pointer;
  font-weight: bold;
  margin-left: 5px;
}
</style>