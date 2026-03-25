import axios from "axios";
import { authStore } from "../stores/auth";

const http = axios.create({
  baseURL: "/api/v1",
  timeout: 15000
});

http.interceptors.request.use((config) => {
  const store = authStore();
  if (store.token.value) {
    config.headers.Authorization = `Bearer ${store.token.value}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && payload.success === false) {
      return Promise.reject(payload); 
    }
    return payload?.data;
  },
  (error) => {
    const serverData = error?.response?.data;
    if (serverData) {
      return Promise.reject(serverData);
    }
    return Promise.reject(new Error(error.message || "Request failed"));
  }
);

export default http;
