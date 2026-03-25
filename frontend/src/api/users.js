import http from "./http";

export function getMyProfile() {
  return http.get("/users/me");
}

export function updateMyProfile(payload) {
  return http.put("/users/me", payload);
}
