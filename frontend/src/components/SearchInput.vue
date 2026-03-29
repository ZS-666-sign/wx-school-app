<script setup>
const props = defineProps({
  modelValue: { type: String, default: "" },
  placeholder: { type: String, default: "搜索..." }
});

const emit = defineEmits(["update:modelValue", "search"]);

function handleInput(event) {
  emit("update:modelValue", event.target.value);
}

function handleKeyup(event) {
  if (event.key === "Enter") {
    emit("search");
  }
}
</script>

<template>
  <div class="search-input">
    <span class="search-icon">⌕</span>
    <input
      :value="modelValue"
      :placeholder="placeholder"
      @input="handleInput"
      @keyup="handleKeyup"
    />
    <button v-if="modelValue" class="clear-btn" @click="emit('update:modelValue', '')">✕</button>
  </div>
</template>

<style scoped>
.search-input {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #dde4ef;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 0 12px;
  height: 42px;
}

.search-icon {
  color: #8894a6;
  font-size: 16px;
  margin-right: 8px;
}

.search-input input {
  flex: 1;
  border: 0;
  background: transparent;
  padding: 0;
  height: 100%;
  font-size: 14px;
}

.search-input input:focus {
  outline: none;
}

.clear-btn {
  border: 0;
  background: #e8ecf2;
  color: #6b7a8d;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-left: 8px;
}

.clear-btn:hover {
  background: #d8dfe8;
}
</style>
