<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { listConversations } from "../api/messages";
import { authStore } from "../stores/auth";
import Loading from "../components/Loading.vue";
import SearchInput from "../components/SearchInput.vue";
import { formatRelativeTime } from "../utils/formatters";
import { handleApiError } from "../utils/errorHandler";

const router = useRouter();
const store = authStore();
const conversations = ref([]);
const info = ref("");
const keyword = ref("");
const loading = ref(false);

const meId = computed(() => store.user.value?.id);

function peerOf(item) {
  if (!meId.value) return item.seller;
  return item.buyer.id === meId.value ? item.seller : item.buyer;
}

const filteredConversations = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  let list = conversations.value;
  if (word) {
    list = list.filter((item) => {
      const peer = peerOf(item);
      return (
        item.goodsTitle?.toLowerCase().includes(word) ||
        peer.nickname?.toLowerCase().includes(word)
      );
    });
  }
  return [...list].sort((a, b) => new Date(b.lastMessageAt) - new Date(a.lastMessageAt));
});

async function loadConversations() {
  loading.value = true;
  try {
    const data = await listConversations({ page: 0, size: 50 });
    conversations.value = data?.items || [];
  } catch (err) {
    info.value = handleApiError(err, "会话加载失败");
  } finally {
    loading.value = false;
  }
}

function goToChat(item) {
  router.push({
    name: "chat",
    params: { goodsId: item.goodsId, conversationId: item.id }
  });
}

onMounted(async () => {
  await loadConversations();
});
</script>

<template>
  <section class="msg-page">
    <SearchInput
      v-model="keyword"
      placeholder="搜索会话..."
    />

    <div class="chat-list">
      <Loading v-if="loading" text="加载中..." />
      <template v-else>
        <button
          v-for="item in filteredConversations"
          :key="item.id"
          class="chat-item"
          @click="goToChat(item)"
        >
          <span class="avatar">{{ peerOf(item).nickname?.slice(0, 1) || "?" }}</span>
          <span class="text">
            <strong>{{ peerOf(item).nickname }}</strong>
            <small>{{ item.goodsTitle }}</small>
          </span>
          <span class="time">{{ formatRelativeTime(item.lastMessageAt) }}</span>
        </button>
        <p v-if="!filteredConversations.length" class="empty">暂无会话</p>
      </template>
    </div>
  </section>
</template>

<style scoped>
.msg-page {
  display: grid;
  gap: 12px;
}

.chat-list {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
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
  cursor: pointer;
  transition: background 0.2s;
}

.chat-item:hover {
  background: #f5f8fc;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  color: #a3a3ae;
  font-size: 12px;
}

.empty {
  text-align: center;
  padding: 18px 10px;
  color: #a2a2ac;
  margin: 0;
}
</style>
