<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { deleteGoods, listMyGoods, updateGoodsStatus } from "../api/goods";
import { uploadImage } from "../api/uploads";
import { getMyProfile, updateMyProfile } from "../api/users";
import { authStore } from "../stores/auth";

const router = useRouter();
const store = authStore();
const info = ref("");
const goodsItems = ref([]);
const avatarInputRef = ref(null);
const avatarUploading = ref(false);

const profile = reactive({
  nickname: "",
  avatarUrl: ""
});

function statusText(status) {
  return status === "ON_SALE" ? "在售中" : "已下架";
}

function formatPrice(value) {
  return Number(value || 0).toFixed(2);
}

function openAvatarPicker() {
  avatarInputRef.value?.click();
}

async function handleAvatarSelect(event) {
  const file = event.target.files?.[0];
  event.target.value = "";
  if (!file) return;

  avatarUploading.value = true;
  info.value = "";
  try {
    const data = await uploadImage(file);
    profile.avatarUrl = data.url;
    info.value = "头像上传成功";
  } catch (err) {
    info.value = err?.message || "头像上传失败，请重试";
  } finally {
    avatarUploading.value = false;
  }
}

async function loadProfile() {
  const data = await getMyProfile();
  profile.nickname = data.nickname;
  profile.avatarUrl = data.avatarUrl || "";
}

async function loadGoods() {
  const data = await listMyGoods({ page: 0, size: 50 });
  goodsItems.value = data.items;
}

async function saveProfile() {
  info.value = "";
  try {
    const updated = await updateMyProfile(profile);
    store.setAuth(store.token.value, updated);
    info.value = "资料已更新";
  } catch (err) {
    info.value = err?.message || "保存失败";
  }
}

async function toggleStatus(item) {
  const nextStatus = item.status === "ON_SALE" ? "OFF_SHELF" : "ON_SALE";
  await updateGoodsStatus(item.id, nextStatus);
  await loadGoods();
}

async function removeGoods(id) {
  await deleteGoods(id);
  await loadGoods();
}

function editGoods(id) {
  router.push(`/publish/${id}`);
}

function logout() {
  store.clearAuth();
  router.push("/auth");
}

onMounted(async () => {
  await loadProfile();
  await loadGoods();
});
</script>

<template>
  <section class="profile-page">
    <article class="paper profile-card">
      <div class="profile-hero">
        <button class="avatar-btn" :disabled="avatarUploading" @click="openAvatarPicker">
          <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="头像" />
          <span v-else>{{ profile.nickname?.slice(0, 1) || "我" }}</span>
        </button>

        <div class="hero-meta">
          <p class="meta-title">昵称</p>
          <input v-model="profile.nickname" class="nick-input" placeholder="请输入昵称" />
          <p class="meta-tip">点击左侧头像可从本地相册选择并上传</p>
        </div>

        <input
          ref="avatarInputRef"
          class="file-input"
          type="file"
          accept="image/*"
          @change="handleAvatarSelect"
        />
      </div>

      <div class="profile-actions">
        <button class="button primary" @click="saveProfile">保存资料</button>
        <button class="button logout-btn" @click="logout">退出登录</button>
      </div>

      <p class="muted message">{{ info }}</p>
    </article>

    <article class="paper goods-card">
      <div class="card-head">
        <h2 class="section-title">我发布的商品</h2>
        <span class="chip">{{ goodsItems.length }} 件</span>
      </div>

      <div v-if="!goodsItems.length" class="muted">还没有发布商品，去发布页试试吧。</div>

      <div v-for="item in goodsItems" :key="item.id" class="goods-line">
        <div>
          <p class="title">{{ item.title }}</p>
          <small class="muted">¥{{ formatPrice(item.price) }} · {{ statusText(item.status) }}</small>
        </div>
        <div class="actions">
          <button class="button" @click="editGoods(item.id)">编辑</button>
          <button class="button" @click="toggleStatus(item)">
            {{ item.status === "ON_SALE" ? "下架" : "上架" }}
          </button>
          <button class="button" @click="removeGoods(item.id)">删除</button>
        </div>
      </div>
    </article>
  </section>
</template>

<style scoped>
.profile-page {
  display: grid;
  gap: 12px;
}

.profile-card,
.goods-card {
  padding: 12px;
}

.profile-hero {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: center;
}

.avatar-btn {
  width: 74px;
  height: 74px;
  border-radius: 999px;
  border: 0;
  padding: 0;
  overflow: hidden;
  background: linear-gradient(145deg, #7dcba0, #6ebf94);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.avatar-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-meta {
  display: grid;
  gap: 6px;
}

.meta-title {
  margin: 0;
  color: #4c5f79;
  font-size: 13px;
}

.nick-input {
  height: 38px;
}

.meta-tip {
  margin: 0;
  color: #7b8798;
  font-size: 12px;
}

.file-input {
  display: none;
}

.profile-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.profile-actions .button {
  flex: 1;
}

.logout-btn {
  color: #7a4a4a;
  border-color: #efd5d5;
  background: #fff7f7;
}

.message {
  min-height: 20px;
  margin: 10px 0 0;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.goods-line {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  border-top: 1px dashed #dedbe5;
  padding: 10px 0;
}

.title {
  margin: 0 0 3px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

@media (min-width: 900px) {
  .profile-page {
    grid-template-columns: 1fr 1.1fr;
  }

  .goods-line {
    grid-template-columns: 1fr auto;
  }
}
</style>
