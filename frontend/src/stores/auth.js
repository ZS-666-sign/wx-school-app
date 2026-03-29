import { computed, ref } from "vue";

const tokenRef = ref(localStorage.getItem("token") || "");
const userRef = ref(
  (() => {
    try {
      const raw = localStorage.getItem("user");
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  })()
);

function setAuth(token, user) {
  tokenRef.value = token;
  userRef.value = user;
  localStorage.setItem("token", token);
  localStorage.setItem("user", JSON.stringify(user));
}

function clearAuth() {
  tokenRef.value = "";
  userRef.value = null;
  localStorage.removeItem("token");
  localStorage.removeItem("user");
}

export function authStore() {
  return {
    token: tokenRef,
    user: userRef,
    isLoggedIn: computed(() => !!tokenRef.value),
    setAuth,
    clearAuth
  };
}
