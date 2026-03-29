<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { listCategories } from "../api/categories";
import { listGoods } from "../api/goods";
import GoodsCard from "../components/GoodsCard.vue";
import SearchInput from "../components/SearchInput.vue";
import Loading from "../components/Loading.vue";
import { handleApiError } from "../utils/errorHandler";

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
    statusText.value = handleApiError(err, "分类加载失败");
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
    goodsItems.value = data?.items || [];
  } catch (err) {
    statusText.value = handleApiError(err, "商品加载失败，请稍后重试");
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
        <SearchInput
          v-model="query.keyword"
          placeholder="搜索你想买的闲置"
          @search="loadGoods(true)"
        />
        <div class="hero-actions">
          <button class="icon-btn" title="刷新" aria-label="刷新" @click="loadGoods(true)">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M19 12a7 7 0 1 1-2.05-4.95"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2.1"
              />
              <path
                d="M19 5v4.6h-4.6"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2.1"
              />
            </svg>
          </button>
          <button class="icon-btn publish-btn" title="发布" aria-label="发布" @click="router.push('/publish')">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle
                cx="12"
                cy="12"
                r="8.2"
                fill="none"
                stroke="currentColor"
                stroke-width="1.9"
              />
              <path
                d="M12 8.3v7.4M8.3 12h7.4"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-width="1.9"
              />
            </svg>
          </button>
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
    <Loading v-if="loading" text="正在加载商品..." />

    <section v-else-if="goodsItems.length" class="waterfall">
      <GoodsCard
        v-for="(item, index) in goodsItems"
        :key="item.id"
        :item="item"
        :height="coverHeight(index)"
        @click="openGoods"
      />
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

.hero-actions {
  display: flex;
  gap: 6px;
}

.icon-btn {
  border: 1px solid #d2dbe9;
  width: 38px;
  height: 38px;
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.94);
  color: #253247;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  line-height: 0;
  cursor: pointer;
}

.icon-btn svg {
  width: 20px;
  height: 20px;
  display: block;
}

.publish-btn {
  color: #f08f57;
}

.publish-btn svg {
  width: 19px;
  height: 19px;
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
  cursor: pointer;
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

@media (min-width: 920px) {
  .waterfall {
    column-count: 4;
    column-gap: 14px;
  }
}
</style>
