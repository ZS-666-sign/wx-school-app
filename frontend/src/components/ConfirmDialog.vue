<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: "确认" },
  message: { type: String, default: "确定要执行此操作吗？" },
  confirmText: { type: String, default: "确定" },
  cancelText: { type: String, default: "取消" },
  type: { type: String, default: "default" } // default | danger
});

const emit = defineEmits(["confirm", "cancel", "update:visible"]);

function handleConfirm() {
  emit("confirm");
  emit("update:visible", false);
}

function handleCancel() {
  emit("cancel");
  emit("update:visible", false);
}

function handleOverlayClick(event) {
  if (event.target === event.currentTarget) {
    handleCancel();
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="dialog-overlay" @click="handleOverlayClick">
        <div class="dialog-content" :class="type">
          <h3 class="dialog-title">{{ title }}</h3>
          <p class="dialog-message">{{ message }}</p>
          <div class="dialog-actions">
            <button class="btn btn-cancel" @click="handleCancel">{{ cancelText }}</button>
            <button class="btn btn-confirm" :class="type" @click="handleConfirm">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.dialog-content {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  width: 100%;
  max-width: 320px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.dialog-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2a37;
}

.dialog-message {
  margin: 0 0 20px;
  font-size: 14px;
  color: #6b7a8d;
  line-height: 1.5;
}

.dialog-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn {
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel {
  border: 1px solid #d8deea;
  background: #fff;
  color: #39465b;
}

.btn-cancel:hover {
  background: #f5f8fc;
}

.btn-confirm {
  border: 0;
  background: var(--brand);
  color: #fff;
}

.btn-confirm:hover {
  opacity: 0.9;
}

.btn-confirm.danger {
  background: var(--danger);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .dialog-content,
.fade-leave-active .dialog-content {
  transition: transform 0.2s ease;
}

.fade-enter-from .dialog-content,
.fade-leave-to .dialog-content {
  transform: scale(0.95);
}
</style>
