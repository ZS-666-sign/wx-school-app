<script setup>
import { computed } from "vue";
import { RouterLink, RouterView, useRoute } from "vue-router";
import { authStore } from "./stores/auth";

const store = authStore();
const route = useRoute();

const navItems = computed(() => {
  const items = [
    { label: "首页", to: "/", auth: false, icon: "首" },
    { label: "消息", to: "/messages", auth: true, icon: "聊" },
    { label: "发布", to: "/publish", auth: true, icon: "发" }
  ];

  items.push(
    store.isLoggedIn.value
      ? { label: "我的", to: "/profile", auth: true, icon: "我" }
      : { label: "登录", to: "/auth", auth: false, icon: "登" }
  );

  return items;
});

const showBottomBar = computed(() => route.name !== "auth");
</script>

<template>
  <div class="app-shell">
    <main class="page-container" :class="{ 'with-tabbar': showBottomBar }">
      <div class="wx-shell" :class="{ auth: route.name === 'auth' }">
        <RouterView />
      </div>
    </main>

    <nav v-if="showBottomBar" class="wx-tabbar">
      <div class="wx-tabbar-inner">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          v-show="!item.auth || store.isLoggedIn.value"
          :to="item.to"
          class="tab-item"
        >
          <span class="tab-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </div>
    </nav>
  </div>
</template>
