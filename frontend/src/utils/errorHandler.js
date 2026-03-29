/**
 * 统一错误处理工具
 */

/**
 * 从错误对象中提取错误消息
 * @param {Error|Object} err - 错误对象
 * @returns {string} 错误消息
 */
export function extractErrorMessage(err) {
  if (!err) return "未知错误";

  // 处理后端返回的结构化错误
  if (err.data && typeof err.data === "object") {
    const values = Object.values(err.data);
    if (values.length) return String(values[0]);
  }

  // 处理普通错误消息
  if (err.message) return err.message;

  return "请求失败，请稍后重试";
}

/**
 * 处理 API 错误并返回友好提示
 * @param {Error|Object} err - 错误对象
 * @param {string} fallback - 默认错误消息
 * @returns {string} 友好的错误消息
 */
export function handleApiError(err, fallback = "操作失败，请稍后重试") {
  if (!err) return fallback;

  // 401 未授权
  if (err.status === 401 || err.message?.includes("401")) {
    return "登录已过期，请重新登录";
  }

  // 403 无权限
  if (err.status === 403 || err.message?.includes("403")) {
    return "没有权限执行此操作";
  }

  // 404 未找到
  if (err.status === 404 || err.message?.includes("404")) {
    return "请求的资源不存在";
  }

  // 429 请求过于频繁
  if (err.status === 429 || err.message?.includes("429")) {
    return "操作过于频繁，请稍后再试";
  }

  // 网络错误
  if (err.message?.includes("Network Error") || err.message?.includes("Failed to fetch")) {
    return "网络连接失败，请检查网络设置";
  }

  // 超时
  if (err.message?.includes("timeout")) {
    return "请求超时，请重试";
  }

  return extractErrorMessage(err) || fallback;
}

/**
 * 通用错误回调
 * @param {Error|Object} err - 错误对象
 * @param {Function} setMessage - 设置消息的函数
 * @param {string} fallback - 默认错误消息
 */
export function onError(err, setMessage, fallback) {
  setMessage(handleApiError(err, fallback));
}
