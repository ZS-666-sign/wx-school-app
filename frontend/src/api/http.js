import axios from "axios";
import { authStore } from "../stores/auth";

const http = axios.create({
  baseURL: "/api/v1",
  timeout: 15000
});

// 请求拦截器：添加 Token
http.interceptors.request.use(
  (config) => {
    const store = authStore();
    if (store.token.value) {
      config.headers.Authorization = `Bearer ${store.token.value}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器：统一处理响应和错误
http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && payload.success === false) {
      return Promise.reject({
        ...payload,
        status: response.status
      });
    }
    return payload?.data;
  },
  (error) => {
    const status = error?.response?.status;
    const serverData = error?.response?.data;

    // 401 未授权：自动登出并跳转登录页
    if (status === 401) {
      const store = authStore();
      store.clearAuth();
      // 使用 window.location 避免 router 未初始化的问题
      if (window.location.pathname !== "/auth") {
        window.location.href = "/auth";
      }
    }

    if (serverData) {
      return Promise.reject({
        ...serverData,
        status
      });
    }

    return Promise.reject(new Error(error.message || "Request failed"));
  }
);

/**
 * 创建可取消的请求
 * @param {Function} requestFn - 请求函数
 * @returns {Object} 包含请求和取消方法的对象
 */
export function createCancellableRequest(requestFn) {
  const controller = new AbortController();
  const request = requestFn({ signal: controller.signal });
  return {
    promise: request,
    cancel: () => controller.abort()
  };
}

export default http;
