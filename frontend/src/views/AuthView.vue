<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { authStore } from "../stores/auth";
import { login, register, resetPassword, sendEmailCode } from "../api/auth";

const router = useRouter();
const store = authStore();
const loading = ref(false);
const message = ref("");
const mode = ref("login");

const registerForm = reactive({
  email: "",
  code: "",
  password: "",
  nickname: ""
});

const loginForm = reactive({
  email: "",
  password: ""
});

const resetForm = reactive({
  email: "",
  code: "",
  newPassword: ""
});

function splitCode(raw) {
  const text = (raw || "").replace(/\D/g, "").slice(0, 6);
  return Array.from({ length: 6 }, (_, i) => text[i] || "");
}

const registerCodeCells = computed(() => splitCode(registerForm.code));
const resetCodeCells = computed(() => splitCode(resetForm.code));

function getErr(err) {
  if (err?.data && typeof err.data === "object") {
    const values = Object.values(err.data);
    if (values.length) return String(values[0]);
  }
  if (err?.message) return err.message;
  return "请求失败，请稍后重试";
}

async function handleSendCode(purpose, email) {
  message.value = "";
  if (!email) {
    message.value = "请先输入邮箱地址";
    return;
  }
  try {
    const data = await sendEmailCode({ email, purpose });
    message.value = data?.debugCode
      ? `开发环境验证码：${data.debugCode}`
      : "验证码已发送，请查收邮箱";
  } catch (err) {
    message.value = getErr(err);
  }
}

async function handleRegister() {
  loading.value = true;
  message.value = "";
  try {
    const data = await register(registerForm);
    store.setAuth(data.token, data.user);
    router.push("/");
  } catch (err) {
    message.value = getErr(err);
  } finally {
    loading.value = false;
  }
}

async function handleLogin() {
  loading.value = true;
  message.value = "";
  try {
    const data = await login(loginForm);
    store.setAuth(data.token, data.user);
    router.push("/");
  } catch (err) {
    message.value = getErr(err);
  } finally {
    loading.value = false;
  }
}

async function handleReset() {
  loading.value = true;
  message.value = "";
  try {
    await resetPassword(resetForm);
    message.value = "密码重置成功，请返回登录";
    mode.value = "login";
    loginForm.email = resetForm.email;
  } catch (err) {
    message.value = getErr(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-gradient"></div>

    <header class="auth-head">
      <button class="back-btn" @click="router.push('/')">返回</button>
      <button class="switch-btn" @click="mode = mode === 'login' ? 'register' : 'login'">
        {{ mode === "login" ? "去注册" : "去登录" }}
      </button>
    </header>

    <div class="hero">
      <p class="hero-sub">校园交易平台</p>
      <h1>{{ mode === "login" ? "欢迎回来" : mode === "register" ? "创建账号" : "重置密码" }}</h1>
      <p class="hero-tip">安工大专属二手平台</p>
    </div>

    <form v-if="mode === 'login'" class="auth-form" @submit.prevent="handleLogin">
      <div class="form-row">
        <label>邮箱</label>
        <input v-model="loginForm.email" placeholder="请输入邮箱" autocomplete="username" required />
      </div>
      <div class="form-row">
        <label>密码</label>
        <input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          autocomplete="current-password"
          required
        />
      </div>
      <button class="button primary full-btn" :disabled="loading">{{ loading ? "登录中..." : "登录" }}</button>
      <button type="button" class="text-link" @click="mode = 'reset'">忘记密码？</button>
    </form>

    <form v-else-if="mode === 'register'" class="auth-form" @submit.prevent="handleRegister">
      <div class="form-row">
        <label>邮箱</label>
        <div class="inline-row">
          <input v-model="registerForm.email" placeholder="请输入邮箱" required />
          <button
            type="button"
            class="button send-btn"
            @click="handleSendCode('REGISTER', registerForm.email)"
          >
            发送码
          </button>
        </div>
      </div>

      <div class="form-row">
        <label>验证码</label>
        <input v-model="registerForm.code" maxlength="6" inputmode="numeric" placeholder="输入6位验证码" required />
      </div>

      <div class="form-row">
        <label>昵称</label>
        <input v-model="registerForm.nickname" placeholder="给自己起个昵称" required />
      </div>
      <div class="form-row">
        <label>密码</label>
        <input v-model="registerForm.password" type="password" placeholder="设置登录密码" required />
      </div>
      <button class="button primary full-btn" :disabled="loading">{{ loading ? "提交中..." : "完成注册" }}</button>
      <button type="button" class="text-link" @click="mode = 'login'">已有账号，去登录</button>
    </form>

    <form v-else class="auth-form" @submit.prevent="handleReset">
      <div class="form-row">
        <label>邮箱</label>
        <div class="inline-row">
          <input v-model="resetForm.email" placeholder="请输入注册邮箱" required />
          <button
            type="button"
            class="button send-btn"
            @click="handleSendCode('RESET_PASSWORD', resetForm.email)"
          >
            发送码
          </button>
        </div>
      </div>

      <div class="form-row">
        <label>验证码</label>
        <div class="code-row">
          <span v-for="(n, i) in resetCodeCells" :key="i" class="code-cell">{{ n || " " }}</span>
        </div>
        <input v-model="resetForm.code" maxlength="6" inputmode="numeric" placeholder="输入6位验证码" required />
      </div>

      <div class="form-row">
        <label>新密码</label>
        <input v-model="resetForm.newPassword" type="password" placeholder="输入新密码" required />
      </div>
      <button class="button primary full-btn" :disabled="loading">{{ loading ? "提交中..." : "重置密码" }}</button>
      <button type="button" class="text-link" @click="mode = 'login'">返回登录</button>
    </form>

    <p class="status-text" :class="{ error: message }">{{ message || " " }}</p>
  </section>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: calc(20px + var(--safe-top)) 20px 24px;
  position: relative;
  overflow: hidden;
}

.auth-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(280px 220px at 85% -40px, rgba(43, 114, 188, 0.2), transparent 70%),
    radial-gradient(300px 220px at -40px 130px, rgba(255, 180, 113, 0.26), transparent 72%),
    linear-gradient(180deg, #f5f8fc 0%, #f9f4ea 100%);
  z-index: -1;
}

.auth-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn,
.switch-btn {
  border: 1px solid #d8e1ee;
  background: rgba(255, 255, 255, 0.86);
  color: #4c5a71;
  border-radius: 12px;
  height: 38px;
  padding: 0 12px;
  font-size: 13px;
}

.hero {
  margin-top: 42px;
}

.hero-sub {
  margin: 0;
  color: #73839c;
  font-size: 12px;
  letter-spacing: 0.06em;
}

.hero h1 {
  margin: 8px 0 8px;
  font-size: 36px;
  line-height: 1.08;
}

.hero-tip {
  margin: 0;
  color: #7d899b;
  font-size: 13px;
}

.auth-form {
  margin-top: 26px;
  padding: 14px;
  border-radius: 20px;
  border: 1px solid #e4ebf4;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 24px rgba(32, 40, 57, 0.06);
}

.inline-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.send-btn {
  border-radius: 12px;
  padding: 0 12px;
  height: 44px;
  font-size: 13px;
}

.full-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  margin-top: 4px;
}

.text-link {
  margin-top: 8px;
  border: 0;
  background: transparent;
  color: #6d7f97;
  font-size: 13px;
  padding: 0;
}

.status-text {
  margin: 14px 0 0;
  min-height: 20px;
  text-align: center;
  color: #667791;
  font-size: 13px;
}

.status-text.error {
  color: var(--danger);
}

@media (min-width: 920px) {
  .auth-page {
    min-height: auto;
    margin-top: 48px;
    border-radius: 26px;
    border: 1px solid #e5ebf4;
    background: rgba(255, 255, 255, 0.72);
    backdrop-filter: blur(8px);
  }
}
</style>
