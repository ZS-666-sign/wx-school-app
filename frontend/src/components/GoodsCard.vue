<script setup>
import { formatPrice } from "../utils/formatters";

const props = defineProps({
  item: { type: Object, required: true },
  height: { type: Number, default: 200 }
});

const emit = defineEmits(["click"]);

function handleClick() {
  emit("click", props.item.id);
}
</script>

<template>
  <article class="goods-card" @click="handleClick">
    <div class="cover" :style="{ height: `${height}px` }">
      <img
        v-if="item.imageUrls?.length"
        :src="item.imageUrls[0]"
        :alt="item.title"
        loading="lazy"
      />
      <div v-else class="cover-empty">暂无图片</div>
      <span class="price-tag">{{ formatPrice(item.price) }}</span>
      <span class="cond-tag">{{ item.conditionLevel || "成色未知" }}</span>
    </div>
    <div class="meta">
      <h3>{{ item.title }}</h3>
      <p>{{ item.campusLocation || "校内交易" }}</p>
    </div>
  </article>
</template>

<style scoped>
.goods-card {
  break-inside: avoid;
  margin-bottom: 12px;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #ece7de;
  box-shadow: 0 8px 16px rgba(26, 34, 51, 0.06);
  cursor: pointer;
  animation: card-in 0.45s ease both;
}

.goods-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px rgba(26, 34, 51, 0.1);
}

.cover {
  position: relative;
  background: #f0f3f8;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-empty {
  height: 100%;
  display: grid;
  place-items: center;
  color: #8c97aa;
  font-size: 13px;
}

.price-tag {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(255, 255, 255, 0.96);
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #1f2a37;
}

.cond-tag {
  position: absolute;
  right: 10px;
  top: 10px;
  background: rgba(30, 94, 166, 0.86);
  color: #fff;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 11px;
}

.meta {
  padding: 10px 10px 12px;
}

.meta h3 {
  margin: 0;
  font-size: 14px;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta p {
  margin: 4px 0 0;
  color: #7d889a;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
