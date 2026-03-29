<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { authStore } from "../stores/auth";
import { login, register, resetPassword, sendEmailCode } from "../api/auth";
import BackButton from "../components/BackButton.vue";
import CodeInput from "../components/CodeInput.vue";
import { handleApiError } from "../utils/errorHandler";
import { CODE_COOLDOWN_SECONDS, EMAIL_REGEX } from "../utils/constants";

const router = useRouter();
const store = authStore();
const loading = ref(false);
const message = ref("");
const mode = ref("login");

// 验证码倒计时
const sendCodeCooldown = ref(0);
const resetCodeCooldown = ref(0);
let sendCodeTimer = null;
let resetCodeTimer = null;

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

// 邮箱格式验证
const isRegisterEmailValid = computed(() => EMAIL_REGEX.test(registerForm.email));
const isResetEmailValid = computed(() => EMAIL_REGEX.test(resetForm.email));
const isLoginEmailValid = computed(() => EMAIL_REGEX.test(loginForm.email));

function startCooldown(type) {
  if (type === "register") {
    sendCodeCooldown.value = CODE_COOLDOWN_SECONDS;
    sendCodeTimer = setInterval(() => {
      sendCodeCooldown.value--;
      if (sendCodeCooldown.value <= 0) {
        clearInterval(sendCodeTimer);
        sendCodeTimer = null;
      }
    }, 1000);
  } else {
    resetCodeCooldown.value = CODE_COOLDOWN_SECONDS;
    resetCodeTimer = setInterval(() => {
      resetCodeCooldown.value--;
      if (resetCodeCooldown.value <= 0) {
        clearInterval(resetCodeTimer);
        resetCodeTimer = null;
      }
    }, 1000);
  }
}

async function handleSendCode(purpose, email, type) {
  message.value = "";
  if (!email) {
    message.value = "请先输入邮箱地址";
    return;
  }
  if (!EMAIL_REGEX.test(email)) {
    message.value = "请输入正确的 qq.com 邮箱";
    return;
  }
  if (type === "register" && sendCodeCooldown.value > 0) {
    message.value = `请等待 ${sendCodeCooldown.value} 秒后重试`;
    return;
  }
  if (type === "reset" && resetCodeCooldown.value > 0) {
    message.value = `请等待 ${resetCodeCooldown.value} 秒后重试`;
    return;
  }

  try {
    const data = await sendEmailCode({ email, purpose });
    startCooldown(type);
    message.value = data?.debugCode
      ? `开发环境验证码：${data.debugCode}`
      : "验证码已发送，请查收邮箱";
  } catch (err) {
    message.value = handleApiError(err, "验证码发送失败");
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
    message.value = handleApiError(err, "注册失败");
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
    message.value = handleApiError(err, "登录失败");
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
    message.value = handleApiError(err, "密码重置失败");
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-gradient"></div>

    <header class="auth-head">
      <BackButton to="/" />
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
        <input
          v-model="loginForm.email"
          placeholder="请输入邮箱"
          autocomplete="username"
          required
        />
        <small v-if="loginForm.email && !isLoginEmailValid" class="field-error">请输入正确的 qq.com 邮箱</small>
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
      <button
        class="button primary full-btn"
        :disabled="loading || !isLoginEmailValid || !loginForm.password"
      >
        {{ loading ? "登录中..." : "登录" }}
      </button>
      <button type="button" class="text-link" @click="mode = 'reset'">忘记密码？</button>
    </form>

    <form v-else-if="mode === 'register'" class="auth-form" @submit.prevent="handleRegister">
      <div class="form-row">
        <label>邮箱</label>
        <div class="inline-row">
          <input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            required
          />
          <button
            type="button"
            class="button send-btn"
            :disabled="sendCodeCooldown > 0 || !isRegisterEmailValid"
            @click="handleSendCode('REGISTER', registerForm.email, 'register')"
          >
            {{ sendCodeCooldown > 0 ? `${sendCodeCooldown}s` : "发送码" }}
          </button>
        </div>
        <small v-if="registerForm.email && !isRegisterEmailValid" class="field-error">请输入正确的 qq.com 邮箱</small>
      </div>

      <div class="form-row">
        <label>验证码</label>
        <CodeInput v-model="registerForm.code" />
      </div>

      <div class="form-row">
        <label>昵称</label>
        <input
          v-model="registerForm.nickname"
          placeholder="给自己起个昵称"
          required
        />
      </div>
      <div class="form-row">
        <label>密码</label>
        <input
          v-model="registerForm.password"
          type="password"
          placeholder="设置登录密码（至少6位）"
          minlength="6"
          required
        />
      </div>
      <button
        class="button primary full-btn"
        :disabled="loading || !isRegisterEmailValid || !registerForm.code || !registerForm.nickname || !registerForm.password"
      >
        {{ loading ? "提交中..." : "完成注册" }}
      </button>
      <button type="button" class="text-link" @click="mode = 'login'">已有账号，去登录</button>
    </form>

    <form v-else class="auth-form" @submit.prevent="handleReset">
      <div class="form-row">
        <label>邮箱</label>
        <div class="inline-row">
          <input
            v-model="resetForm.email"
            placeholder="请输入注册邮箱"
            required
          />
          <button
            type="button"
            class="button send-btn"
            :disabled="resetCodeCooldown > 0 || !isResetEmailValid"
            @click="handleSendCode('RESET_PASSWORD', resetForm.email, 'reset')"
          >
            {{ resetCodeCooldown > 0 ? `${resetCodeCooldown}s` : "发送码" }}
          </button>
        </div>
        <small v-if="resetForm.email && !isResetEmailValid" class="field-error">请输入正确的 qq.com 邮箱</small>
      </div>

      <div class="form-row">
        <label>验证码</label>
        <CodeInput v-model="resetForm.code" />
      </div>

      <div class="form-row">
        <label>新密码</label>
        <input
          v-model="resetForm.newPassword"
          type="password"
          placeholder="输入新密码（至少6位）"
          minlength="6"
          required
        />
      </div>
      <button
        class="button primary full-btn"
        :disabled="loading || !isResetEmailValid || !resetForm.code || !resetForm.newPassword"
      >
        {{ loading ? "提交中..." : "重置密码" }}
      </button>
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

.switch-btn {
  border: 1px solid #d8e1ee;
  background: rgba(255, 255, 255, 0.86);
  color: #4c5a71;
  border-radius: 12px;
  height: 38px;
  padding: 0 12px;
  font-size: 13px;
  cursor: pointer;
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
  cursor: pointer;
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

.field-error {
  display: block;
  color: var(--danger);
  font-size: 12px;
  margin-top: 4px;
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
