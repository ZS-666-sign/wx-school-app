import http from "./http";

export function sendEmailCode(payload) {
  return http.post("/auth/email-code", payload);
}

export function register(payload) {
  return http.post("/auth/register", payload);
}

export function login(payload) {
  return http.post("/auth/login", payload);
}

export function resetPassword(payload) {
  return http.post("/auth/reset-password", payload);
}

export function fetchMe() {
  return http.get("/auth/me");
}
