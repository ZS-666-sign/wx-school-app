import http from "./http";

export function startConversation(goodsId) {
  return http.post("/messages/conversations/start", { goodsId });
}

export function listConversations(params) {
  return http.get("/messages/conversations", { params });
}

export function listMessages(conversationId, params) {
  return http.get(`/messages/conversations/${conversationId}/items`, { params });
}

export function sendMessage(payload) {
  return http.post("/messages/items", payload);
}
