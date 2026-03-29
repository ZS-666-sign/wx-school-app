<script setup>
import { useRouter } from "vue-router";

const props = defineProps({
  to: { type: String, default: "/" },
  text: { type: String, default: "返回" }
});

const router = useRouter();

function goBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push(props.to);
  }
}
</script>

<template>
  <button class="back-button" @click="goBack">
    {{ text }}
  </button>
</template>

<style scoped>
.back-button {
  border: 1px solid #d8dfeb;
  background: rgba(255, 255, 255, 0.9);
  color: #4f5d72;
  border-radius: 12px;
  height: 38px;
  padding: 0 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-button:hover {
  background: #f5f8fc;
  border-color: #c8d4e4;
}
</style>
