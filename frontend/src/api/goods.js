import http from "./http";

export function listGoods(params) {
  return http.get("/goods", { params });
}

export function getGoodsDetail(id) {
  return http.get(`/goods/${id}`);
}

export function createGoods(payload) {
  return http.post("/goods", payload);
}

export function updateGoods(id, payload) {
  return http.put(`/goods/${id}`, payload);
}

export function updateGoodsStatus(id, status) {
  return http.patch(`/goods/${id}/status`, { status });
}

export function deleteGoods(id) {
  return http.delete(`/goods/${id}`);
}

export function listMyGoods(params) {
  return http.get("/goods/mine", { params });
}
