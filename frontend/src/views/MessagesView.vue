<script setup>
import { computed, onMounted, ref } from "vue";
import { listConversations, listMessages, sendMessage } from "../api/messages";
import { authStore } from "../stores/auth";

const store = authStore();
const conversations = ref([]);
const currentConversation = ref(null);
const records = ref([]);
const draft = ref("");
const info = ref("");
const keyword = ref("");

const meId = computed(() => store.user.value?.id);

function peerOf(item) {
  if (!meId.value) return item.seller;
  return item.buyer.id === meId.value ? item.seller : item.buyer;
}

function formatTime(value) {
  if (!value) return "";
  const date = new Date(value);
  return date.toLocaleString("zh-CN", { month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit" });
}

const filteredConversations = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  if (!word) return conversations.value;
  return conversations.value.filter((item) => {
    const peer = peerOf(item);
    return (
      item.goodsTitle.toLowerCase().includes(word) ||
      peer.nickname.toLowerCase().includes(word)
    );
  });
});

async function loadConversations() {
  const data = await listConversations({ page: 0, size: 50 });
  conversations.value = data.items;
}

async function chooseConversation(item) {
  currentConversation.value = item;
  const data = await listMessages(item.id, { page: 0, size: 100 });
  records.value = data.items;
}

async function submit() {
  if (!currentConversation.value || !draft.value.trim()) return;
  info.value = "";
  try {
    await sendMessage({
      conversationId: currentConversation.value.id,
      content: draft.value
    });
    draft.value = "";
    await chooseConversation(currentConversation.value);
    await loadConversations();
  } catch (err) {
    info.value = err.message;
  }
}

function isMine(msg) {
  return meId.value && msg.sender.id === meId.value;
}

onMounted(async () => {
  await loadConversations();
});
</script>

<template>
  <section class="msg-page">
    <div class="search-wrap">
      <input v-model="keyword" placeholder="搜索" />
      <span class="search-icon">⌕</span>
    </div>

    <div class="top-chats">
      <h3>置顶聊天</h3>
      <div class="top-list">
        <button
          v-for="item in filteredConversations.slice(0, 8)"
          :key="item.id"
          class="top-user"
          @click="chooseConversation(item)"
        >
          <span class="avatar">{{ peerOf(item).nickname.slice(0, 1) }}</span>
          <span class="name">{{ peerOf(item).nickname }}</span>
        </button>
      </div>
    </div>

    <div class="chat-list">
      <button
        v-for="item in filteredConversations"
        :key="item.id"
        class="chat-item"
        @click="chooseConversation(item)"
      >
        <span class="avatar">{{ peerOf(item).nickname.slice(0, 1) }}</span>
        <span class="text">
          <strong>{{ peerOf(item).nickname }}</strong>
          <small>{{ item.goodsTitle }}</small>
        </span>
        <span class="time">{{ formatTime(item.lastMessageAt) }}</span>
      </button>
      <p v-if="!filteredConversations.length" class="empty">暂无会话</p>
    </div>

    <section v-if="currentConversation" class="chat-panel">
      <h3>{{ currentConversation.goodsTitle }}</h3>
      <div class="record-list">
        <div v-for="msg in records" :key="msg.id" class="record-item" :class="{ mine: isMine(msg) }">
          <small>{{ msg.sender.nickname }}</small>
          <p>{{ msg.content }}</p>
        </div>
      </div>
      <div class="composer">
        <textarea v-model="draft" placeholder="发送消息..." />
        <button class="button primary" @click="submit">发送</button>
      </div>
      <p class="status">{{ info }}</p>
    </section>
  </section>
</template>

<style scoped>
.msg-page {
  display: grid;
  gap: 12px;
}

.search-wrap {
  position: relative;
  background: #f5f5f7;
  border-radius: 999px;
  padding: 0 42px 0 14px;
}

.search-wrap input {
  border: 0;
  height: 42px;
}

.search-icon {
  position: absolute;
  right: 14px;
  top: 10px;
  color: #7a7a84;
  font-size: 20px;
}

.top-chats {
  background: #ececf3;
  border-radius: 18px;
  padding: 12px;
}

.top-chats h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.top-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
}

.top-user {
  border: 0;
  background: transparent;
  padding: 0;
  display: grid;
  justify-items: center;
  gap: 6px;
  min-width: 64px;
}

.avatar {
  width: 54px;
  height: 54px;
  border-radius: 999px;
  background: linear-gradient(145deg, #ff6d8a, #ffb2c1);
  color: #fff;
  font-size: 23px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.name {
  font-size: 13px;
}

.chat-list {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
}

.chat-item {
  width: 100%;
  border: 0;
  border-bottom: 1px solid #f0f0f4;
  background: transparent;
  padding: 11px 12px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
}

.chat-item:last-child {
  border-bottom: 0;
}

.text {
  display: grid;
  text-align: left;
  gap: 2px;
}

.text strong {
  font-size: 16px;
  line-height: 1.2;
}

.text small {
  color: #848490;
  font-size: 13px;
}

.time {
  color: #a3a3ae;
  font-size: 12px;
}

.empty {
  text-align: center;
  padding: 18px 10px;
  color: #a2a2ac;
}

.chat-panel {
  background: #fff;
  border-radius: 18px;
  padding: 12px;
  display: grid;
  gap: 10px;
}

.chat-panel h3 {
  margin: 0;
  font-size: 16px;
}

.record-list {
  max-height: 250px;
  overflow: auto;
  display: grid;
  gap: 7px;
  padding: 2px;
}

.record-item {
  display: grid;
  gap: 3px;
}

.record-item small {
  color: #9797a2;
  font-size: 11px;
}

.record-item p {
  margin: 0;
  background: #f4f4f8;
  border-radius: 11px;
  padding: 8px 10px;
  font-size: 14px;
}

.record-item.mine {
  justify-items: end;
}

.record-item.mine p {
  background: #fee9ef;
}

.composer {
  display: grid;
  gap: 7px;
}

.status {
  margin: 0;
  color: #888894;
  font-size: 12px;
}

@media (min-width: 920px) {
  .msg-page {
    grid-template-columns: 0.92fr 1.08fr;
    gap: 14px;
  }

  .search-wrap,
  .top-chats,
  .chat-list {
    grid-column: 1;
  }

  .chat-panel {
    grid-column: 2;
    grid-row: 1 / span 3;
    align-self: start;
  }
}
</style>
