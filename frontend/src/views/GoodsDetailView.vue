<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getGoodsDetail } from "../api/goods";
import { listMessages, sendMessage, startConversation } from "../api/messages";
import { authStore } from "../stores/auth";

const route = useRoute();
const router = useRouter();
const store = authStore();
const goods = ref(null);
const loading = ref(false);
const info = ref("");
const conversationId = ref(null);
const messages = ref([]);
const draft = ref("");

const currentUserId = computed(() => store.user.value?.id);

function formatPrice(value) {
  return Number(value || 0).toFixed(2);
}

async function loadGoods() {
  loading.value = true;
  info.value = "";
  try {
    goods.value = await getGoodsDetail(route.params.id);
  } catch (err) {
    info.value = err.message;
  } finally {
    loading.value = false;
  }
}

async function beginConversation() {
  info.value = "";
  if (!store.isLoggedIn.value) {
    router.push("/auth");
    return;
  }
  try {
    const data = await startConversation(goods.value.id);
    conversationId.value = data.id;
    await loadMessages();
  } catch (err) {
    info.value = err.message;
  }
}

async function loadMessages() {
  if (!conversationId.value) return;
  const data = await listMessages(conversationId.value, { page: 0, size: 50 });
  messages.value = data.items;
}

async function submitMessage() {
  if (!draft.value.trim() || !conversationId.value) return;
  try {
    await sendMessage({
      conversationId: conversationId.value,
      content: draft.value
    });
    draft.value = "";
    await loadMessages();
  } catch (err) {
    info.value = err.message;
  }
}

onMounted(loadGoods);
</script>

<template>
  <section v-if="loading" class="muted">正在加载商品详情...</section>
  <section v-else-if="goods" class="detail-page">
    <article class="paper goods-card">
      <div class="cover-box">
        <img :src="goods.imageUrls?.[0]" alt="商品图片" />
      </div>
      <div v-if="goods.imageUrls?.length > 1" class="thumb-row">
        <img v-for="url in goods.imageUrls.slice(1)" :key="url" :src="url" alt="商品图" />
      </div>
      <h1 class="section-title">{{ goods.title }}</h1>
      <p class="price">￥{{ formatPrice(goods.price) }}</p>
      <p class="muted">{{ goods.conditionLevel }} · {{ goods.campusLocation }}</p>
      <p class="desc">{{ goods.description }}</p>
      <p class="muted seller">发布者：{{ goods.seller.nickname }}</p>
    </article>

    <article class="paper chat-card">
      <h2 class="chat-title">站内沟通</h2>
      <p class="muted tip">先在平台聊清楚，再约校内当面交易。</p>
      <button class="button primary" @click="beginConversation">发起对话</button>
      <p class="muted">{{ info }}</p>

      <div v-if="conversationId" class="chat-wrap">
        <div class="chat-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-item"
            :class="{ me: currentUserId && msg.sender.id === currentUserId }"
          >
            <p class="sender">{{ msg.sender.nickname }}</p>
            <p class="bubble">{{ msg.content }}</p>
          </div>
        </div>
        <div class="composer">
          <textarea v-model="draft" placeholder="输入你想问的问题，例如：今晚 7 点图书馆门口可以吗？" />
          <button class="button" @click="submitMessage">发送</button>
        </div>
      </div>
    </article>
  </section>
</template>

<style scoped>
.detail-page {
  display: grid;
  gap: 12px;
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

.chat-wrap {
  margin-top: 10px;
  display: grid;
  gap: 8px;
}

.chat-list {
  background: #fff;
  border: 1px solid #dcd9e2;
  border-radius: 12px;
  max-height: 300px;
  overflow: auto;
  padding: 8px;
  display: grid;
  gap: 8px;
}

.msg-item {
  display: grid;
  gap: 3px;
}

.msg-item.me {
  justify-items: end;
}

.sender {
  margin: 0;
  font-size: 12px;
  color: #8d8598;
}

.bubble {
  margin: 0;
  background: #f6f4fa;
  border: 1px solid #e2deea;
  border-radius: 10px;
  padding: 7px 10px;
  max-width: 90%;
  line-height: 1.45;
}

.msg-item.me .bubble {
  background: #fceef2;
  border-color: #f1c9d5;
}

.composer {
  display: grid;
  gap: 6px;
}
</style>
