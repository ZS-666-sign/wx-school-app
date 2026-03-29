<script setup>
defineProps({
  text: { type: String, default: "加载中..." },
  fullScreen: { type: Boolean, default: false }
});
</script>

<template>
  <div class="loading-wrapper" :class="{ 'full-screen': fullScreen }">
    <div class="loading-spinner">
      <div class="spinner"></div>
    </div>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<style scoped>
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 12px;
}

.loading-wrapper.full-screen {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  z-index: 999;
}

.loading-spinner {
  width: 32px;
  height: 32px;
}

.spinner {
  width: 100%;
  height: 100%;
  border: 3px solid #e8ecf2;
  border-top-color: var(--brand);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-text {
  margin: 0;
  font-size: 13px;
  color: #6b7a8d;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
