<script setup>
import { computed, ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: String, default: "" },
  length: { type: Number, default: 6 }
});

const emit = defineEmits(["update:modelValue"]);

const inputRef = ref(null);
const focused = ref(false);
const focusedIndex = ref(-1);

const cells = computed(() => {
  const raw = (props.modelValue || "").replace(/\D/g, "").slice(0, props.length);
  return Array.from({ length: props.length }, (_, i) => raw[i] || "");
});

const isComplete = computed(() => {
  return cells.value.every((cell) => cell !== "");
});

function focusInput() {
  inputRef.value?.focus();
}

function handleFocus() {
  focused.value = true;
  focusedIndex.value = Math.min(props.modelValue?.length || 0, props.length - 1);
}

function handleBlur() {
  focused.value = false;
  focusedIndex.value = -1;
}

function handleInput(event) {
  const value = event.target.value.replace(/\D/g, "").slice(0, props.length);
  emit("update:modelValue", value);
  focusedIndex.value = Math.min(value.length, props.length - 1);
}

function handleKeydown(event) {
  if (event.key === "Backspace" && !props.modelValue && focusedIndex.value > 0) {
    focusedIndex.value--;
  }
}

function handleCellClick(index) {
  focusedIndex.value = index;
  focusInput();
}
</script>

<template>
  <div class="code-input-wrapper" @click="focusInput">
    <div class="code-cells">
      <div
        v-for="(cell, index) in cells"
        :key="index"
        class="code-cell"
        :class="{
          filled: cell !== '',
          focused: focused && focusedIndex === index
        }"
        @click.stop="handleCellClick(index)"
      >
        <span v-if="cell" class="cell-value">{{ cell }}</span>
        <span v-if="focused && focusedIndex === index && cell === ''" class="cursor"></span>
      </div>
    </div>
    <input
      ref="inputRef"
      type="tel"
      inputmode="numeric"
      :maxlength="length"
      :value="modelValue"
      class="hidden-input"
      @focus="handleFocus"
      @blur="handleBlur"
      @input="handleInput"
      @keydown="handleKeydown"
    />
  </div>
</template>

<style scoped>
.code-input-wrapper {
  position: relative;
  display: inline-block;
  cursor: text;
}

.code-cells {
  display: flex;
  gap: 6px;
}

.code-cell {
  width: 38px;
  height: 44px;
  border: 1.5px solid #e0e5ec;
  border-radius: 10px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.2s ease;
  cursor: pointer;
}

.code-cell:hover {
  border-color: #c2daf6;
}

.code-cell.focused {
  border-color: var(--brand);
  box-shadow: 0 0 0 2.5px rgba(30, 94, 166, 0.12);
}

.code-cell.filled {
  border-color: var(--brand);
  background: #f0f6ff;
}

.cell-value {
  font-size: 18px;
  font-weight: 600;
  color: #1f2a37;
  line-height: 1;
}

.cursor {
  position: absolute;
  width: 1.5px;
  height: 20px;
  background: var(--brand);
  border-radius: 1px;
  animation: blink 1s infinite;
}

.hidden-input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
