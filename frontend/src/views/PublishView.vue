<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { listCategories } from "../api/categories";
import { createGoods, getGoodsDetail, updateGoods } from "../api/goods";
import { uploadImage } from "../api/uploads";
import { handleApiError } from "../utils/errorHandler";
import { GOODS_MAX_IMAGES } from "../utils/constants";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const imageUploading = ref(false);
const uploadProgress = ref({ current: 0, total: 0 });
const message = ref("");
const categories = ref([]);
const isEdit = computed(() => !!route.params.id);
const selectedCategory = ref("");
const imageInputRef = ref(null);
const showCategoryPicker = ref(false);

const form = reactive({
  title: "",
  description: "",
  price: "",
  conditionLevel: "九成新",
  campusLocation: "",
  categoryId: "",
  imageUrls: []
});

const conditionLevels = ["全新", "九成新", "八成新", "七成新", "六成新及以下"];
const imageList = computed(() => form.imageUrls);

function setCategory(id) {
  selectedCategory.value = String(id);
  form.categoryId = id;
  showCategoryPicker.value = false;
}

function openImagePicker() {
  imageInputRef.value?.click();
}

function removeImage(index) {
  form.imageUrls.splice(index, 1);
}

async function handleImageSelect(event) {
  const files = Array.from(event.target.files || []);
  event.target.value = "";
  if (!files.length) return;

  const remain = GOODS_MAX_IMAGES - form.imageUrls.length;
  if (remain <= 0) {
    message.value = `最多上传 ${GOODS_MAX_IMAGES} 张图片`;
    return;
  }

  const targetFiles = files.slice(0, remain);
  message.value = files.length > remain ? `最多${GOODS_MAX_IMAGES}张，已选前${remain}张` : "";

  imageUploading.value = true;
  uploadProgress.value = { current: 0, total: targetFiles.length };

  try {
    const urls = await Promise.all(
      targetFiles.map(async (file, i) => {
        const data = await uploadImage(file);
        uploadProgress.value.current = i + 1;
        return data.url;
      })
    );
    form.imageUrls.push(...urls);
    if (!message.value) message.value = "";
  } catch (err) {
    message.value = handleApiError(err, "图片上传失败");
  } finally {
    imageUploading.value = false;
  }
}

async function loadCategories() {
  try {
    categories.value = await listCategories();
  } catch (err) {
    message.value = handleApiError(err, "分类加载失败");
  }
}

async function loadEditData() {
  if (!isEdit.value) return;
  try {
    const data = await getGoodsDetail(route.params.id);
    form.title = data.title || "";
    form.description = data.description || "";
    form.price = data.price;
    form.conditionLevel = data.conditionLevel || "九成新";
    form.campusLocation = data.campusLocation || "";
    form.categoryId = data.category?.id || "";
    selectedCategory.value = String(data.category?.id || "");
    form.imageUrls = [...(data.imageUrls || [])];
  } catch (err) {
    message.value = handleApiError(err, "商品数据加载失败");
  }
}

async function submit() {
  message.value = "";
  if (!form.imageUrls.length) { message.value = "请至少上传1张图片"; return; }
  if (!form.title.trim()) { message.value = "请输入商品标题"; return; }
  if (!form.price || Number(form.price) <= 0) { message.value = "请输入有效价格"; return; }

  loading.value = true;
  try {
    const payload = {
      title: form.title.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      conditionLevel: form.conditionLevel,
      campusLocation: form.campusLocation.trim(),
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      imageUrls: form.imageUrls
    };
    const data = isEdit.value
      ? await updateGoods(route.params.id, payload)
      : await createGoods(payload);
    router.push(`/goods/${data.id}`);
  } catch (err) {
    message.value = handleApiError(err, "提交失败");
  } finally {
    loading.value = false;
  }
}

function getCategoryName() {
  if (!form.categoryId) return "";
  return categories.value.find(c => c.id === form.categoryId)?.name || "";
}

onMounted(async () => {
  await loadCategories();
  await loadEditData();
});
</script>

<template>
  <div class="publish-page">
    <!-- 顶部 -->
    <header class="top-bar">
      <button class="back-btn" @click="$router.back()">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <h1>{{ isEdit ? "编辑商品" : "发闲置" }}</h1>
      <div style="width:22px"></div>
    </header>

    <!-- 图片区 -->
    <section class="photo-section">
      <div class="photo-grid">
        <div v-for="(url, i) in imageList" :key="i" class="photo-item">
          <img :src="url" alt="" />
          <button class="del-btn" @click="removeImage(i)">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
          </button>
          <span v-if="i === 0" class="cover-label">封面</span>
        </div>
        <button v-if="imageList.length < GOODS_MAX_IMAGES" class="photo-add" :disabled="imageUploading" @click="openImagePicker">
          <template v-if="imageUploading">
            <div class="spinner"></div>
            <span>{{ uploadProgress.current }}/{{ uploadProgress.total }}</span>
          </template>
          <template v-else>
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5"><path d="M12 5v14M5 12h14"/></svg>
          </template>
        </button>
      </div>
      <input ref="imageInputRef" type="file" class="hidden-input" accept="image/jpeg,image/png,image/webp" multiple @change="handleImageSelect" />
    </section>

    <!-- 标题 -->
    <section class="form-section">
      <div class="form-row title-row">
        <input v-model="form.title" placeholder="请输入宝贝标题" maxlength="120" />
        <span class="counter">{{ form.title.length }}/120</span>
      </div>

      <!-- 描述 -->
      <div class="form-row desc-row">
        <textarea v-model="form.description" placeholder="描述一下宝贝的品牌型号、购入时间、瑕疵情况、转手原因..." maxlength="5000" rows="5" />
      </div>
    </section>

    <!-- 价格 -->
    <section class="form-section">
      <div class="form-row price-row">
        <label>价格</label>
        <div class="price-input">
          <span class="yen">¥</span>
          <input v-model="form.price" type="number" min="0.01" step="0.01" placeholder="0.00" />
        </div>
      </div>
    </section>

    <!-- 分类 -->
    <section class="form-section">
      <div class="form-row clickable" @click="showCategoryPicker = !showCategoryPicker">
        <label>分类</label>
        <div class="row-value">
          <span :class="{ empty: !getCategoryName() }">{{ getCategoryName() || "请选择" }}</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
        </div>
      </div>
      <div v-if="showCategoryPicker" class="picker-panel">
        <button class="picker-item" :class="{ active: !form.categoryId }" @click="setCategory('')">未分类</button>
        <button v-for="c in categories" :key="c.id" class="picker-item" :class="{ active: form.categoryId === c.id }" @click="setCategory(c.id)">{{ c.name }}</button>
      </div>
    </section>

    <!-- 成色 -->
    <section class="form-section">
      <div class="form-row">
        <label>成色</label>
        <div class="chip-group">
          <button v-for="lv in conditionLevels" :key="lv" class="chip" :class="{ active: form.conditionLevel === lv }" @click="form.conditionLevel = lv">{{ lv }}</button>
        </div>
      </div>
    </section>

    <!-- 交易地点 -->
    <section class="form-section">
      <div class="form-row">
        <label>交易地点</label>
        <input v-model="form.campusLocation" placeholder="如：图书馆门口、食堂" maxlength="120" />
      </div>
    </section>

    <!-- 底部按钮 -->
    <footer class="bottom-bar">
      <p v-if="message" class="msg" :class="{ err: message }">{{ message }}</p>
      <button class="publish-btn" :disabled="loading || imageUploading" @click="submit">
        {{ loading ? "发布中..." : isEdit ? "保存" : "发布" }}
      </button>
    </footer>
  </div>
</template>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120px;
}

/* 顶栏 */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  padding-top: calc(12px + var(--safe-top));
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}
.top-bar h1 { margin: 0; font-size: 17px; font-weight: 600; }
.back-btn { background: none; border: none; padding: 4px; cursor: pointer; color: #333; }

/* 图片区 */
.photo-section {
  background: #fff;
  padding: 12px 16px;
  margin-top: 8px;
}
.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.photo-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
}
.photo-item img { width: 100%; height: 100%; object-fit: cover; }
.del-btn {
  position: absolute; top: 4px; right: 4px;
  width: 20px; height: 20px; border-radius: 50%;
  border: none; background: rgba(0,0,0,.5); color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
}
.cover-label {
  position: absolute; bottom: 0; left: 0;
  background: #ff6b3d; color: #fff; font-size: 10px;
  padding: 2px 6px; border-radius: 0 6px 0 0;
}
.photo-add {
  aspect-ratio: 1; border: 2px dashed #ddd; border-radius: 8px;
  background: #fafafa; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 4px;
  color: #bbb; cursor: pointer;
}
.photo-add:hover { border-color: #bbb; }
.photo-add span { font-size: 11px; color: #999; }
.spinner {
  width: 20px; height: 20px;
  border: 2px solid #eee; border-top-color: #1e5ea6;
  border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.hidden-input { display: none; }

/* 表单 */
.form-section {
  background: #fff;
  margin-top: 8px;
  padding: 0 16px;
}
.form-row {
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.form-row:last-child { border-bottom: none; }
.form-row label { display: block; font-size: 14px; color: #333; margin-bottom: 8px; }
.form-row input, .form-row textarea {
  width: 100%; border: none; font-size: 15px; padding: 0;
  background: transparent; outline: none; resize: none;
}
.form-row input::placeholder, .form-row textarea::placeholder { color: #ccc; }

.title-row { display: flex; align-items: flex-start; gap: 8px; }
.title-row input { flex: 1; font-size: 17px; }
.counter { font-size: 11px; color: #ccc; flex-shrink: 0; }

.price-row { display: flex; align-items: center; gap: 12px; }
.price-row label { margin: 0; flex-shrink: 0; }
.price-input { display: flex; align-items: center; flex: 1; }
.yen { font-size: 22px; font-weight: 700; color: #ff4444; margin-right: 4px; }
.price-input input { font-size: 22px; font-weight: 700; color: #ff4444; }

.clickable { display: flex; align-items: center; justify-content: space-between; cursor: pointer; }
.clickable label { margin: 0; }
.row-value { display: flex; align-items: center; gap: 4px; color: #333; }
.row-value .empty { color: #ccc; }

/* 分类选择器 */
.picker-panel {
  display: grid; grid-template-columns: repeat(3, 1fr);
  gap: 8px; padding: 12px 0;
}
.picker-item {
  padding: 10px 8px; border: 1px solid #eee; border-radius: 8px;
  background: #fff; font-size: 13px; color: #666; cursor: pointer;
}
.picker-item.active { background: #1e5ea6; border-color: #1e5ea6; color: #fff; }

/* 成色 */
.chip-group { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  padding: 8px 16px; border: 1px solid #eee; border-radius: 20px;
  background: #fff; font-size: 13px; color: #666; cursor: pointer;
}
.chip.active { background: #1e5ea6; border-color: #1e5ea6; color: #fff; }

/* 底部 */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: rgba(255,255,255,.95); backdrop-filter: blur(10px);
  border-top: 1px solid #f0f0f0;
  padding: 10px 16px; padding-bottom: calc(10px + var(--safe-bottom));
  z-index: 20;
}
.msg { margin: 0 0 8px; font-size: 12px; color: #999; text-align: center; }
.msg.err { color: #ff4444; }
.publish-btn {
  width: 100%; height: 48px; border: none; border-radius: 24px;
  background: linear-gradient(135deg, #2b72bc, #1e5ea6);
  color: #fff; font-size: 16px; font-weight: 600; cursor: pointer;
  box-shadow: 0 4px 12px rgba(30,94,166,.3);
}
.publish-btn:disabled { background: #ccc; box-shadow: none; cursor: not-allowed; }
</style>
