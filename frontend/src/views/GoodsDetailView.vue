<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getGoodsDetail } from "../api/goods";
import { authStore } from "../stores/auth";
import Loading from "../components/Loading.vue";
import BackButton from "../components/BackButton.vue";
import { formatPrice } from "../utils/formatters";
import { handleApiError } from "../utils/errorHandler";

const route = useRoute();
const router = useRouter();
const store = authStore();
const goods = ref(null);
const loading = ref(false);
const info = ref("");

async function loadGoods() {
  loading.value = true;
  info.value = "";
  try {
    goods.value = await getGoodsDetail(route.params.id);
  } catch (err) {
    info.value = handleApiError(err, "商品详情加载失败");
  } finally {
    loading.value = false;
  }
}

function goToChat() {
  if (!store.isLoggedIn.value) {
    router.push("/auth");
    return;
  }
  router.push({
    name: "chat",
    params: { goodsId: goods.value.id }
  });
}

onMounted(loadGoods);
</script>

<template>
  <Loading v-if="loading" text="正在加载商品详情..." />
  <section v-else-if="goods" class="detail-page">
    <div class="detail-header">
      <BackButton to="/" />
    </div>

    <article class="paper goods-card">
      <div class="cover-box">
        <img
          :src="goods.imageUrls?.[0]"
          :alt="goods.title"
          loading="lazy"
        />
      </div>
      <div v-if="goods.imageUrls?.length > 1" class="thumb-row">
        <img
          v-for="url in goods.imageUrls.slice(1)"
          :key="url"
          :src="url"
          :alt="goods.title"
          loading="lazy"
        />
      </div>
      <h1 class="section-title">{{ goods.title }}</h1>
      <p class="price">{{ formatPrice(goods.price, 2) }}</p>
      <p class="muted">{{ goods.conditionLevel }} · {{ goods.campusLocation }}</p>
      <p class="desc">{{ goods.description }}</p>
      <p class="muted seller">发布者：{{ goods.seller.nickname }}</p>
    </article>

    <article class="paper chat-card">
      <h2 class="chat-title">站内沟通</h2>
      <p class="muted tip">先在平台聊清楚，再约校内当面交易。</p>
      <button class="button primary chat-btn" @click="goToChat">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        发起对话
      </button>
      <p v-if="info" class="muted">{{ info }}</p>
    </article>
  </section>
  <section v-else class="muted">{{ info || "商品不存在" }}</section>
</template>

<style scoped>
.detail-page {
  display: grid;
  gap: 12px;
}

.detail-header {
  margin-bottom: 4px;
}

.goods-card,
.chat-card {
  padding: 12px;
}

.cover-box {
  height: 220px;
  background: #f3f1f7;
  border-radius: 14px;
  overflow: hidden;
}

.cover-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  margin-top: 8px;
}

.thumb-row img {
  width: 100%;
  height: 74px;
  object-fit: cover;
  border-radius: 10px;
}

.price {
  margin: 0;
  color: #d5718f;
  font-size: 22px;
  font-weight: 700;
}

.desc {
  margin-top: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.seller {
  margin-bottom: 0;
}

.chat-title {
  margin: 0 0 6px;
  font-size: 17px;
}

.tip {
  margin-top: 0;
}

.chat-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
