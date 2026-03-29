<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goHome() {
  router.push("/");
}
</script>

<template>
  <section class="not-found-page">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-title">页面未找到</p>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除</p>
      <button class="button primary" @click="goHome">返回首页</button>
    </div>
  </section>
</template>

<style scoped>
.not-found-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.not-found-content {
  text-align: center;
  padding: 40px 20px;
}

.error-code {
  margin: 0;
  font-size: 80px;
  font-weight: 700;
  color: var(--brand);
  line-height: 1;
  opacity: 0.3;
}

.error-title {
  margin: 16px 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2a37;
}

.error-desc {
  margin: 0 0 24px;
  font-size: 14px;
  color: #6b7a8d;
}

.button {
  padding: 12px 24px;
}
</style>
