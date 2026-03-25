import http from "./http";

export function listCategories() {
  return http.get("/categories");
}
