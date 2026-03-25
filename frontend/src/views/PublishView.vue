<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { listCategories } from "../api/categories";
import { createGoods, getGoodsDetail, updateGoods } from "../api/goods";
import { uploadImage } from "../api/uploads";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const imageUploading = ref(false);
const message = ref("");
const categories = ref([]);
const isEdit = computed(() => !!route.params.id);
const selectedCategory = ref("");
const imageInputRef = ref(null);

const form = reactive({
  title: "",
  description: "",
  price: "",
  conditionLevel: "九成新",
  campusLocation: "",
  categoryId: "",
  imageUrls: []
});

const imageList = computed(() => form.imageUrls);

function setCategory(id) {
  selectedCategory.value = String(id);
  form.categoryId = id;
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

  const remain = 9 - form.imageUrls.length;
  if (remain <= 0) {
    message.value = "最多上传 9 张图片";
    return;
  }

  const targetFiles = files.slice(0, remain);
  if (files.length > remain) {
    message.value = `最多上传 9 张图片，已选择前 ${remain} 张`;
  } else {
    message.value = "";
  }

  imageUploading.value = true;
  try {
    for (const file of targetFiles) {
      const data = await uploadImage(file);
      form.imageUrls.push(data.url);
    }
    if (!message.value) {
      message.value = "图片上传成功";
    }
  } catch (err) {
    message.value = err?.message || "图片上传失败，请稍后重试";
  } finally {
    imageUploading.value = false;
  }
}

async function loadCategories() {
  categories.value = await listCategories();
}

async function loadEditData() {
  if (!isEdit.value) return;
  const data = await getGoodsDetail(route.params.id);
  form.title = data.title || "";
  form.description = data.description || "";
  form.price = data.price;
  form.conditionLevel = data.conditionLevel || "九成新";
  form.campusLocation = data.campusLocation || "";
  form.categoryId = data.category?.id || "";
  selectedCategory.value = String(data.category?.id || "");
  form.imageUrls = [...(data.imageUrls || [])];
}

async function submit() {
  message.value = "";
  if (!form.imageUrls.length) {
    message.value = "请至少上传 1 张商品图片";
    return;
  }

  loading.value = true;
  try {
    const payload = {
      title: form.title.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      conditionLevel: form.conditionLevel.trim(),
      campusLocation: form.campusLocation.trim(),
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      imageUrls: imageList.value
    };

    const data = isEdit.value
      ? await updateGoods(route.params.id, payload)
      : await createGoods(payload);

    router.push(`/goods/${data.id}`);
  } catch (err) {
    message.value = err?.message || "提交失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadCategories();
  await loadEditData();
});
</script>

<template>
  <section class="publish-page">
    <header class="publish-head">
      <button class="go-back" @click="router.push('/')">返回</button>
      <div>
        <p class="head-sub">CAMPUS MARKET</p>
        <h1>{{ isEdit ? "编辑商品" : "发布闲置" }}</h1>
      </div>
    </header>

    <section class="paper intro-card">
      <p>把商品描述写清楚，能显著提升成交效率。建议附上真实图片和可交易时间。</p>
    </section>

    <section class="paper form-card">
      <h2>基础信息</h2>
      <div class="form-row">
        <label>商品标题</label>
        <input v-model="form.title" placeholder="例如：高数教材（近全新）" required />
      </div>
      <div class="grid-two">
        <div class="form-row">
          <label>价格（元）</label>
          <input v-model="form.price" type="number" min="0.01" step="0.01" placeholder="99" required />
        </div>
        <div class="form-row">
          <label>成色</label>
          <input v-model="form.conditionLevel" placeholder="九成新" required />
        </div>
      </div>
      <div class="form-row">
        <label>校内交易地点</label>
        <input v-model="form.campusLocation" placeholder="图书馆门口 / 食堂门口" required />
      </div>
      <div class="form-row">
        <label>商品描述</label>
        <textarea
          v-model="form.description"
          placeholder="写清品牌、购入时间、是否有瑕疵、是否可议价..."
          required
        />
      </div>
    </section>

    <section class="paper form-card">
      <h2>分类与图片</h2>

      <div class="chips">
        <button
          class="chip-btn"
          :class="{ active: selectedCategory === '' }"
          @click="setCategory('')"
        >
          未分类
        </button>
        <button
          v-for="item in categories"
          :key="item.id"
          class="chip-btn"
          :class="{ active: selectedCategory === String(item.id) }"
          @click="setCategory(item.id)"
        >
          {{ item.name }}
        </button>
      </div>

      <div class="upload-box">
        <label>商品图片（最多 9 张）</label>
        <div class="upload-actions">
          <button
            type="button"
            class="button"
            :disabled="imageUploading"
            @click="openImagePicker"
          >
            {{ imageUploading ? "上传中..." : "从相册选择" }}
          </button>
          <small class="muted">支持 jpg / jpeg / png / webp / gif</small>
        </div>
        <input
          ref="imageInputRef"
          class="file-input"
          type="file"
          accept="image/*"
          multiple
          @change="handleImageSelect"
        />
      </div>

      <div v-if="imageList.length" class="preview-grid">
        <div v-for="(url, index) in imageList" :key="`${url}-${index}`" class="preview-item">
          <img :src="url" alt="预览图" />
          <button type="button" class="remove-img" @click="removeImage(index)">删除</button>
        </div>
      </div>
    </section>

    <footer class="submit-bar">
      <p class="status" :class="{ error: message }">{{ message || " " }}</p>
      <button class="button primary submit-btn" :disabled="loading || imageUploading" @click="submit">
        {{ loading ? "提交中..." : isEdit ? "保存修改" : "确认发布" }}
      </button>
    </footer>
  </section>
</template>

<style scoped>
.publish-page {
  display: grid;
  gap: 12px;
}

.publish-head {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 12px;
}

.go-back {
  border: 1px solid #d8dfeb;
  background: rgba(255, 255, 255, 0.9);
  color: #4f5d72;
  border-radius: 12px;
  height: 38px;
  padding: 0 12px;
  font-size: 13px;
}

.publish-head h1 {
  margin: 2px 0 0;
  font-size: 24px;
}

.head-sub {
  margin: 0;
  color: #8090a6;
  letter-spacing: 0.08em;
  font-size: 11px;
}

.intro-card {
  padding: 12px 14px;
  background: linear-gradient(145deg, #ebf5ff 0%, #f6fbff 100%);
}

.intro-card p {
  margin: 0;
  color: #5d6f88;
  font-size: 13px;
  line-height: 1.5;
}

.form-card {
  padding: 14px;
}

.form-card h2 {
  margin: 0 0 12px;
  font-size: 18px;
}

.grid-two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.chip-btn {
  border: 1px solid #dce3ee;
  border-radius: 999px;
  background: #f7faff;
  color: #607086;
  height: 34px;
  padding: 0 14px;
}

.chip-btn.active {
  background: #1e5ea6;
  border-color: #1e5ea6;
  color: #fff;
}

.upload-box {
  display: grid;
  gap: 8px;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.file-input {
  display: none;
}

.preview-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.preview-item {
  position: relative;
}

.preview-item img {
  width: 100%;
  height: 92px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e6edf6;
}

.remove-img {
  position: absolute;
  right: 6px;
  bottom: 6px;
  border: 0;
  border-radius: 999px;
  background: rgba(29, 43, 63, 0.86);
  color: #fff;
  font-size: 11px;
  line-height: 1;
  padding: 6px 8px;
}

.submit-bar {
  position: sticky;
  bottom: 6px;
  padding: 10px;
  border-radius: 18px;
  border: 1px solid #e5ebf3;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(6px);
  display: grid;
  gap: 8px;
}

.status {
  min-height: 18px;
  margin: 0;
  font-size: 12px;
  color: #6f7d91;
}

.status.error {
  color: var(--danger);
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
}

@media (max-width: 680px) {
  .grid-two {
    grid-template-columns: 1fr;
  }
}

@media (min-width: 920px) {
  .publish-page {
    grid-template-columns: 1.1fr 0.9fr;
    align-items: start;
    gap: 14px;
  }

  .publish-head,
  .intro-card {
    grid-column: 1 / span 2;
  }

  .submit-bar {
    grid-column: 1 / span 2;
    bottom: 10px;
  }
}
</style>
