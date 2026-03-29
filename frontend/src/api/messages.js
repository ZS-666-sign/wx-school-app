import http from "./http";

export function startConversation(goodsId) {
  return http.post("/messages/conversations", { goodsId });
}

export function listConversations(params) {
  return http.get("/messages/conversations", { params });
}

export function listMessages(conversationId, params) {
  return http.get(`/messages/conversations/${conversationId}/messages`, { params });
}

export function sendMessage(payload) {
  return http.post("/messages/messages", payload);
}
