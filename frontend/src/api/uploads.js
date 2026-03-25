import http from "./http";

export function uploadImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  return http.post("/uploads/image", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
}
