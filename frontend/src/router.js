import { createRouter, createWebHistory } from "vue-router";
import { authStore } from "./stores/auth";

const HomeView = () => import("./views/HomeView.vue");
const AuthView = () => import("./views/AuthView.vue");
const GoodsDetailView = () => import("./views/GoodsDetailView.vue");
const PublishView = () => import("./views/PublishView.vue");
const MessagesView = () => import("./views/MessagesView.vue");
const ProfileView = () => import("./views/ProfileView.vue");

const routes = [
  { path: "/", name: "home", component: HomeView, meta: { title: "首页" } },
  { path: "/auth", name: "auth", component: AuthView, meta: { guestOnly: true, title: "账号登录" } },
  { path: "/goods/:id", name: "goods-detail", component: GoodsDetailView, meta: { title: "商品详情" } },
  { path: "/publish", name: "publish", component: PublishView, meta: { requiresAuth: true, title: "发布商品" } },
  { path: "/publish/:id", name: "edit-goods", component: PublishView, meta: { requiresAuth: true, title: "编辑商品" } },
  { path: "/messages", name: "messages", component: MessagesView, meta: { requiresAuth: true, title: "消息" } },
  { path: "/profile", name: "profile", component: ProfileView, meta: { requiresAuth: true, title: "我的" } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const store = authStore();
  if (to.meta.requiresAuth && !store.isLoggedIn.value) {
    return { name: "auth" };
  }
  if (to.meta.guestOnly && store.isLoggedIn.value) {
    return { name: "home" };
  }
  return true;
});

export default router;
