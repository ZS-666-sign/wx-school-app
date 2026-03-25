<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { listCategories } from "../api/categories";
import { listGoods } from "../api/goods";

const router = useRouter();
const loading = ref(false);
const categories = ref([]);
const goodsItems = ref([]);
const statusText = ref("");

const query = reactive({
  keyword: "",
  categoryId: "",
  page: 0,
  size: 24,
  status: "ON_SALE"
});

const categoryItems = computed(() => [
  { id: "", name: "推荐" },
  ...categories.value
]);

function formatPrice(value) {
  return `¥${Number(value || 0).toFixed(0)}`;
}

function coverHeight(index) {
  const heights = [184, 236, 196, 244, 206, 220];
  return heights[index % heights.length];
}

function chooseCategory(id) {
  query.categoryId = id;
  loadGoods(true);
}

function openGoods(id) {
  router.push(`/goods/${id}`);
}

async function loadCategories() {
  try {
    categories.value = await listCategories();
  } catch (err) {
    statusText.value = err?.message || "分类加载失败";
  }
}

async function loadGoods(resetPage = false) {
  if (resetPage) query.page = 0;
  loading.value = true;
  statusText.value = "";
  try {
    const data = await listGoods({
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      status: query.status,
      page: query.page,
      size: query.size
    });
    goodsItems.value = data.items || [];
  } catch (err) {
    statusText.value = err?.message || "商品加载失败，请稍后重试";
    goodsItems.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadCategories();
  await loadGoods();
});
</script>

<template>
  <section class="home-page">
    <header class="paper home-hero">
      <div class="hero-top">
        <div class="search-box">
          <span class="search-ic">⌕</span>
          <input
            v-model="query.keyword"
            placeholder="搜索你想买的闲置"
            @keyup.enter="loadGoods(true)"
          />
        </div>
        <div class="hero-actions">
          <button class="icon-btn" title="刷新" @click="loadGoods(true)">刷</button>
          <button class="icon-btn solid" title="发布" @click="router.push('/publish')">发</button>
        </div>
      </div>
      <p class="hero-title">校园闲置集市</p>
      <p class="hero-sub">发现同校在售物品，面对面更安心</p>
    </header>

    <section class="category-strip">
      <button
        v-for="item in categoryItems"
        :key="String(item.id)"
        class="category-pill"
        :class="{ active: String(query.categoryId) === String(item.id) }"
        @click="chooseCategory(item.id)"
      >
        {{ item.name }}
      </button>
    </section>

    <p v-if="statusText" class="status-line">{{ statusText }}</p>
    <p v-if="loading" class="status-line">正在加载商品...</p>

    <section v-else-if="goodsItems.length" class="waterfall">
      <article
        v-for="(item, index) in goodsItems"
        :key="item.id"
        class="goods-card"
        @click="openGoods(item.id)"
      >
        <div class="cover" :style="{ height: `${coverHeight(index)}px` }">
          <img v-if="item.imageUrls?.length" :src="item.imageUrls[0]" alt="商品图片" />
          <div v-else class="cover-empty">暂无图片</div>
          <span class="price-tag">{{ formatPrice(item.price) }}</span>
          <span class="cond-tag">{{ item.conditionLevel || "成色未知" }}</span>
        </div>
        <div class="meta">
          <h3>{{ item.title }}</h3>
          <p>{{ item.campusLocation || "校内交易" }}</p>
        </div>
      </article>
    </section>

    <section v-else class="empty-state paper">
      <h3>暂时没有匹配的商品</h3>
      <p>换个关键词或分类试试</p>
    </section>
  </section>
</template>

<style scoped>
.home-page {
  display: grid;
  gap: 14px;
}

.home-hero {
  padding: 14px;
  background:
    linear-gradient(150deg, #fff7e6 0%, #f8f3ea 45%, #edf5ff 100%),
    #ffffff;
}

.hero-top {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 10px;
}

.search-box {
  height: 40px;
  border: 1px solid #dde4ef;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  padding: 0 10px;
  gap: 8px;
}

.search-ic {
  color: #8894a6;
  font-size: 16px;
}

.search-box input {
  border: 0;
  background: transparent;
  padding: 0;
  height: 100%;
  font-size: 14px;
}

.hero-actions {
  display: flex;
  gap: 6px;
}

.icon-btn {
  border: 1px solid #d2dbe9;
  width: 36px;
  height: 36px;
  border-radius: 11px;
  background: #fff;
  color: #58667b;
  font-size: 13px;
}

.icon-btn.solid {
  background: #1e5ea6;
  border-color: #1e5ea6;
  color: #fff;
}

.hero-title {
  margin: 14px 0 4px;
  font-size: 24px;
  letter-spacing: 0.01em;
}

.hero-sub {
  margin: 0;
  font-size: 13px;
  color: #768195;
}

.category-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.category-strip::-webkit-scrollbar {
  display: none;
}

.category-pill {
  flex: 0 0 auto;
  border: 1px solid #dde4ef;
  border-radius: 999px;
  height: 34px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.8);
  color: #5e6d83;
  font-size: 13px;
}

.category-pill.active {
  background: #1e5ea6;
  border-color: #1e5ea6;
  color: #fff;
  font-weight: 600;
}

.status-line {
  margin: 0;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.7);
  color: #6c788b;
  font-size: 13px;
}

.waterfall {
  column-count: 2;
  column-gap: 12px;
}

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
}

.meta p {
  margin: 4px 0 0;
  color: #7d889a;
  font-size: 12px;
}

.empty-state {
  padding: 20px 14px;
  text-align: center;
}

.empty-state h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.empty-state p {
  margin: 0;
  color: #7f8b9f;
  font-size: 13px;
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

@media (min-width: 920px) {
  .waterfall {
    column-count: 4;
    column-gap: 14px;
  }
}
</style>
