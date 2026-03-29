<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getGoodsDetail } from "../api/goods";
import { listMessages, sendMessage, startConversation } from "../api/messages";
import { authStore } from "../stores/auth";
import { formatRelativeTime } from "../utils/formatters";
import { handleApiError } from "../utils/errorHandler";

const route = useRoute();
const router = useRouter();
const store = authStore();

const goods = ref(null);
const conversationId = ref(null);
const messages = ref([]);
const draft = ref("");
const loading = ref(true);
const sending = ref(false);
const info = ref("");
const chatListRef = ref(null);
const inputRef = ref(null);

const currentUserId = computed(() => store.user.value?.id);

function isMine(msg) {
  return currentUserId.value && msg.sender?.id === currentUserId.value;
}

function formatTime(dateStr) {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  const now = new Date();
  const diff = now - d;
  const pad = (n) => String(n).padStart(2, "0");

  if (diff < 60000) return "刚刚";

  const hours = d.getHours();
  const timeStr = `${pad(hours)}:${pad(d.getMinutes())}`;

  if (d.toDateString() === now.toDateString()) return timeStr;

  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (d.toDateString() === yesterday.toDateString()) return `昨天 ${timeStr}`;

  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${timeStr}`;
}

function scrollToBottom(smooth = true) {
  nextTick(() => {
    if (chatListRef.value) {
      chatListRef.value.scrollTo({
        top: chatListRef.value.scrollHeight,
        behavior: smooth ? "smooth" : "instant"
      });
    }
  });
}

async function init() {
  loading.value = true;
  info.value = "";
  try {
    const goodsId = route.params.goodsId;
    goods.value = await getGoodsDetail(goodsId);

    if (route.params.conversationId) {
      conversationId.value = route.params.conversationId;
    } else {
      const data = await startConversation(goodsId);
      conversationId.value = data.id;
      router.replace({
        name: "chat",
        params: { goodsId, conversationId: data.id }
      });
    }

    await loadMessages();
  } catch (err) {
    info.value = handleApiError(err, "加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadMessages() {
  if (!conversationId.value) return;
  try {
    const data = await listMessages(conversationId.value, { page: 0, size: 100 });
    messages.value = data?.items || [];
    scrollToBottom(false);
  } catch (err) {
    info.value = handleApiError(err, "消息加载失败");
  }
}

async function submitMessage() {
  const content = draft.value.trim();
  if (!content || !conversationId.value || sending.value) return;

  sending.value = true;
  try {
    await sendMessage({
      conversationId: conversationId.value,
      content
    });
    draft.value = "";
    await loadMessages();
    inputRef.value?.focus();
  } catch (err) {
    info.value = handleApiError(err, "发送失败");
  } finally {
    sending.value = false;
  }
}

function handleKeydown(e) {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    submitMessage();
  }
}

function goBack() {
  router.back();
}

function viewGoods() {
  if (goods.value) {
    router.push(`/goods/${goods.value.id}`);
  }
}

onMounted(() => {
  if (!store.isLoggedIn.value) {
    router.push("/auth");
    return;
  }
  init();
});

watch(messages, () => scrollToBottom());
</script>

<template>
  <div class="chat-page">
    <!-- 顶部导航栏 -->
    <header class="chat-header">
      <button class="back-btn" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="header-info" @click="viewGoods">
        <div class="header-avatar">
          <img v-if="goods?.seller?.avatarUrl" :src="goods.seller.avatarUrl" :alt="goods.seller.nickname" />
          <span v-else class="avatar-placeholder">{{ goods?.seller?.nickname?.[0] || "?" }}</span>
        </div>
        <div class="header-text">
          <h1 class="header-name">{{ goods?.seller?.nickname || "对话" }}</h1>
          <p class="header-sub" v-if="goods">{{ goods.title }}</p>
        </div>
      </div>
      <button class="more-btn" @click="viewGoods">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="1"/><circle cx="12" cy="5" r="1"/><circle cx="12" cy="19" r="1"/>
        </svg>
      </button>
    </header>

    <!-- 商品卡片 -->
    <div class="goods-bar" v-if="goods" @click="viewGoods">
      <img v-if="goods.imageUrls?.[0]" :src="goods.imageUrls[0]" :alt="goods.title" class="goods-thumb" />
      <div class="goods-info">
        <p class="goods-title">{{ goods.title }}</p>
        <p class="goods-price">¥{{ goods.price }}</p>
      </div>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M9 18l6-6-6-6"/>
      </svg>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-body" ref="chatListRef">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <p>开始聊天吧</p>
        <p class="empty-tip">说点什么，问问商品细节？</p>
      </div>

      <template v-else>
        <div
          v-for="(msg, index) in messages"
          :key="msg.id"
          class="message-row"
          :class="{ mine: isMine(msg) }"
        >
          <!-- 左侧：对方头像或占位 -->
          <div class="avatar-col">
            <div v-if="!isMine(msg)" class="msg-avatar">
              <img v-if="msg.sender?.avatarUrl" :src="msg.sender.avatarUrl" :alt="msg.sender.nickname" />
              <span v-else class="avatar-placeholder small">{{ msg.sender?.nickname?.[0] || "?" }}</span>
            </div>
          </div>

          <!-- 中间：气泡 -->
          <div class="bubble-col">
            <span class="msg-name" v-if="!isMine(msg)">{{ msg.sender?.nickname }}</span>
            <div class="msg-bubble" :class="{ mine: isMine(msg) }">
              <p>{{ msg.content }}</p>
            </div>
            <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
          </div>

          <!-- 右侧：自己头像或占位 -->
          <div class="avatar-col">
            <div v-if="isMine(msg)" class="msg-avatar mine">
              <img v-if="msg.sender?.avatarUrl" :src="msg.sender.avatarUrl" :alt="msg.sender.nickname" />
              <span v-else class="avatar-placeholder small">{{ msg.sender?.nickname?.[0] || "?" }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部输入栏 -->
    <footer class="chat-footer">
      <div class="input-area">
        <textarea
          ref="inputRef"
          v-model="draft"
          placeholder="发消息..."
          rows="1"
          @keydown="handleKeydown"
        ></textarea>
        <button
          class="send-btn"
          :class="{ active: draft.trim() }"
          :disabled="sending || !draft.trim()"
          @click="submitMessage"
        >
          <svg v-if="!sending" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
          <div v-else class="send-loading"></div>
        </button>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background: #f5f5f5;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
}

/* 顶部导航 */
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  padding-top: calc(12px + var(--safe-top));
  background: linear-gradient(135deg, #2b72bc 0%, #1e5ea6 100%);
  color: #fff;
  min-height: 56px;
}

.back-btn,
.more-btn {
  background: none;
  border: none;
  color: #fff;
  padding: 8px;
  margin: -8px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.back-btn:hover,
.more-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.header-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  cursor: pointer;
}

.header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.2);
}

.header-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.avatar-placeholder.small {
  font-size: 12px;
}

.header-text {
  min-width: 0;
}

.header-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  opacity: 0.8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 商品栏 */
.goods-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  cursor: pointer;
  transition: background 0.2s;
}

.goods-bar:hover {
  background: #fafafa;
}

.goods-thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.goods-info {
  flex: 1;
  min-width: 0;
}

.goods-title {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-price {
  margin: 4px 0 0;
  font-size: 15px;
  font-weight: 700;
  color: #e74c3c;
}

/* 聊天区域 */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  -webkit-overflow-scrolling: touch;
}

.loading-state,
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 8px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e8e8e8;
  border-top-color: #1e5ea6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.empty-tip {
  font-size: 13px;
  color: #bbb;
}

/* 消息行 - 使用三列布局确保对齐 */
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

/* 对方消息：左对齐 */
/* 自己消息：右对齐 */
.message-row.mine {
  justify-content: flex-end;
}

/* 头像列 */
.avatar-col {
  width: 40px;
  flex-shrink: 0;
}

/* 自己消息时左侧头像列不占空间 */
.message-row.mine .avatar-col:first-child {
  width: 0;
}

/* 气泡列自适应 */
.bubble-col {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

/* 自己的消息：气泡文字右对齐 */
.message-row.mine .bubble-col {
  align-items: flex-end;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: #e8e8e8;
}

.msg-avatar.mine {
  background: #d8e8fa;
}

.msg-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.msg-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  padding-left: 4px;
}

.msg-bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  word-break: break-word;
  line-height: 1.5;
}

.msg-bubble p {
  margin: 0;
  font-size: 15px;
  white-space: pre-wrap;
}

.msg-bubble.mine {
  background: #95ec69;
  border-bottom-left-radius: 18px;
  border-bottom-right-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  padding: 0 4px;
}

/* 底部输入栏 */
.chat-footer {
  background: #fff;
  border-top: 1px solid #e8e8e8;
  padding: 8px 12px;
  padding-bottom: calc(8px + var(--safe-bottom));
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.input-area textarea {
  flex: 1;
  border: none;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 15px;
  resize: none;
  max-height: 120px;
  line-height: 1.4;
  min-height: 40px;
  outline: none;
  transition: background 0.2s;
}

.input-area textarea:focus {
  background: #efefef;
}

.input-area textarea::placeholder {
  color: #aaa;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #e8e8e8;
  color: #aaa;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.send-btn.active {
  background: linear-gradient(135deg, #2b72bc 0%, #1e5ea6 100%);
  color: #fff;
}

.send-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.send-loading {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
</style>
