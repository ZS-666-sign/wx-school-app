<script setup>
defineProps({
  message: { type: Object, required: true },
  isMine: { type: Boolean, default: false }
});
</script>

<template>
  <div class="message-bubble" :class="{ mine: isMine }">
    <p class="sender">{{ message.sender.nickname }}</p>
    <p class="content">{{ message.content }}</p>
  </div>
</template>

<style scoped>
.message-bubble {
  display: grid;
  gap: 3px;
}

.message-bubble.mine {
  justify-items: end;
}

.sender {
  margin: 0;
  font-size: 12px;
  color: #8d8598;
}

.content {
  margin: 0;
  background: #f6f4fa;
  border: 1px solid #e2deea;
  border-radius: 10px;
  padding: 7px 10px;
  max-width: 90%;
  line-height: 1.45;
  word-break: break-word;
}

.message-bubble.mine .content {
  background: #fceef2;
  border-color: #f1c9d5;
}
</style>
