import http from "./http";

const MAX_UPLOAD_BYTES = 8 * 1024 * 1024;
const MAX_DIMENSION = 2560;
const INITIAL_QUALITY = 0.82;
const MIN_QUALITY = 0.5;
const QUALITY_STEP = 0.08;
const ALLOWED_MIME_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

function buildCompressedName(originalName, targetType) {
  const base = (originalName || "image").replace(/\.[^.]+$/, "");
  const ext = targetType === "image/webp" ? "webp" : "jpg";
  return `${base}.${ext}`;
}

function loadImage(file) {
  return new Promise((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file);
    const image = new Image();
    image.onload = () => {
      URL.revokeObjectURL(objectUrl);
      resolve(image);
    };
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error("Failed to read image"));
    };
    image.src = objectUrl;
  });
}

function getTargetSize(width, height) {
  const maxSide = Math.max(width, height);
  if (maxSide <= MAX_DIMENSION) {
    return { width, height };
  }
  const ratio = MAX_DIMENSION / maxSide;
  return {
    width: Math.max(1, Math.round(width * ratio)),
    height: Math.max(1, Math.round(height * ratio))
  };
}

function canvasToBlob(canvas, type, quality) {
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error("Failed to compress image"));
        return;
      }
      resolve(blob);
    }, type, quality);
  });
}

async function compressImageForUpload(file) {
  if (!file?.type?.startsWith("image/")) {
    return file;
  }
  if (!ALLOWED_MIME_TYPES.has(file.type)) {
    throw new Error("Only jpg/jpeg/png/webp images are supported");
  }
  if (file.size <= MAX_UPLOAD_BYTES) {
    return file;
  }

  const image = await loadImage(file);
  const targetSize = getTargetSize(image.naturalWidth, image.naturalHeight);
  const canvas = document.createElement("canvas");
  canvas.width = targetSize.width;
  canvas.height = targetSize.height;

  const context = canvas.getContext("2d");
  if (!context) {
    return file;
  }
  context.drawImage(image, 0, 0, targetSize.width, targetSize.height);

  let quality = INITIAL_QUALITY;
  let blob = await canvasToBlob(canvas, "image/webp", quality);

  while (blob.size > MAX_UPLOAD_BYTES && quality > MIN_QUALITY) {
    quality = Math.max(MIN_QUALITY, Number((quality - QUALITY_STEP).toFixed(2)));
    blob = await canvasToBlob(canvas, "image/webp", quality);
  }

  return new File([blob], buildCompressedName(file.name, "image/webp"), { type: "image/webp" });
}

export function uploadImage(file) {
  return compressImageForUpload(file).then((processedFile) => {
    if (processedFile.size > MAX_UPLOAD_BYTES) {
      throw new Error("Image is still too large after compression. Please choose a smaller image.");
    }

    const formData = new FormData();
    formData.append("file", processedFile);
    return http.post("/uploads/image", formData, {
      headers: {
        "Content-Type": "multipart/form-data"
      },
      timeout: 60000
    });
  });
}
